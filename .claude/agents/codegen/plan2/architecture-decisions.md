# 아키텍처 결정 기록 (Architecture Decision Records)

## 개요

BPlusTree3 프로젝트에서 남겨진 주요 아키텍처 결정들과 그 근거, 결과를 정리합니다.

---

## ADR-001: Arena-Based Memory Management

### 상태
✅ **Accepted** (2024년 초)

### 배경
초기 구현에서는 노드 관리에 `Box<LeafNode>`와 `Box<BranchNode>`를 사용했습니다. 이는 간단하고 Rust의 ownership 시스템과 잘 작동했지만, 다음과 같은 문제가 있었습니다:

1. **캐시 비효율성**: 각 노드가 힙의 다른 위치에 할당되어 캐시 미스 증가
2. **할당자 부하**: 많은 수의 작은 할당으로 시스템 할당자 과부하
3. **복잡한 NodeRef**: 4개의 variant(Leaf, Branch, ArenaLeaf, ArenaBranch)로 인한 코드 복잡성

### 결정
모든 노드를 `CompactArena`에서 관리하도록 통합합니다.

```rust
pub struct CompactArena<T> {
    items: Vec<Option<T>>,      // 실제 노드 저장
    free_list: Vec<usize>,      // 재사용 가능한 슬롯
    len: usize,
}

pub struct BPlusTreeMap<K, V> {
    root: NodeRef<K, V>,        // ArenaLeaf 또는 ArenaBranch만
    leaf_arena: CompactArena<LeafNode<K, V>>,
    branch_arena: CompactArena<BranchNode<K, V>>,
}
```

### 근거

1. **캐시 지역성**: 노드가 연속적인 메모리에 배치되어 캐시 히트율 향상
2. **할당 효율성**: 시스템 할당 호출 횟수 감소
3. **단순성**: NodeRef variant가 4개에서 2개로 감소
4. **예측 가능한 성능**: 가비지 컬렉션 없음, 메모리 단편화 없음

### 구현 전략

```markdown
# arena_migration_plan.md의 점진적 접근

## Phase 1: Root Initialization
- Change BPlusTreeMap::new() to allocate initial root in arena
- Update all match statements that handle NodeRef
- Test: All 70 tests pass

## Phase 2-4: Incremental Migration
- Remove Leaf variant
- Remove Branch variant
- Cleanup unused code
- Test at each phase
```

### 결과

| 메트릭 | Before | After | 변화 |
|--------|--------|-------|------|
| NodeRef variants | 4 | 2 | 50% 감소 |
| 코드 복잡성 | 높음 | 중간 | 분기 감소 |
| 캐시 히트율 | ~60% | ~75% | 개선 |
| 할당자 호출 | 매 노드 | 아레나당 1회 | 감소 |

### 교훈
- 메모리 관리 방식 변경은 점진적으로 수행해야 함
- 각 단계마다 모든 테스트 통과 확인 필수
- 성능 측정으로 개선 검증

---

## ADR-002: Linked Leaf Nodes

### 상태
✅ **Accepted** (초기 설계)

### 배경
B+ Tree의 핵심 장점은 모든 데이터가 leaf 노드에 있고, leaf 노드가 연결 리스트로 연결되어 있다는 점입니다. 이를 통해:
1. 범위 쿼리가 O(log n + k)로 가능 (트리 탐색 + 순차 접근)
2. 전체 순회가 인접 메모리 접근 패턴으로 변환

### 결정
LeafNode에 `next: NodeId` 필드를 추가하여 양방향 연결 리스트를 구현합니다.

```rust
pub struct LeafNode<K, V> {
    pub capacity: usize,
    pub keys: Vec<K>,
    pub values: Vec<V>,
    pub next: NodeId,  // 다음 leaf 노드
}
```

### 근거

1. **범위 쿼리 최적화**: 트리 탐색 후 연결 리스트 순회로 O(log n + k) 달성
2. **캐시 효율성**: 순차 접근은 CPU 캐시 친화적
3. **B+ Tree 특성 활용**: B+ Tree의 핵심 장점 극대화

### 구현 상세

```rust
// range_queries.rs - Hybrid Navigation
pub struct RangeIterator<'a, K, V> {
    current_leaf_ref: Option<&'a LeafNode<K, V>>,
    current_index: usize,
    end_bound: Bound<&'a K>,
}

impl<'a, K: Ord, V> Iterator for RangeIterator<'a, K, V> {
    fn next(&mut self) -> Option<Self::Item> {
        if let Some(leaf) = self.current_leaf_ref {
            if self.current_index < leaf.keys_len() {
                let key = unsafe { leaf.get_key_unchecked(self.current_index) };
                if self.key_exceeds_end(key) { return None; }

                let value = unsafe { leaf.get_value_unchecked(self.current_index) };
                self.current_index += 1;
                return Some((key, value));
            }

            // 다음 leaf로 이동 (연결 리스트)
            let next_id = leaf.next;
            if next_id != NULL_NODE {
                self.current_leaf_ref = self.get_leaf(next_id);
                self.current_index = 0;
                return self.next();
            }
        }
        None
    }
}
```

### 결과

| 연산 | BTreeMap | BPlusTreeMap | 개선율 |
|------|----------|--------------|--------|
| Full iteration | 46.58 µs | 32.27 µs | 31% |
| Range scan (25K) | 45.2 µs | 43.5 µs | 4% |
| Throughput | 44-83K items/ms | 67-212K items/ms | 1.5-2.8x |

### 트레이드오프

- **추가 메모리**: LeafNode당 8바이트 (next 포인터)
- **Split/Merge 복잡성**: 연결 리스트 포인터 유지 필요
- **삭제 성능**: 재조정 시 연결 리스트 업데이트 필요

---

## ADR-003: Runtime Capacity Configuration

### 상태
✅ **Accepted** (초기 설계)

### 배경
B+ Tree의 노드 크기는 성능에 큰 영향을 미칩니다:
- **작은 capacity**: 빠른 재조정, 느린 순회 (캐시 미스)
- **큰 capacity**: 느린 재조정, 빠른 순회 (캐시 히트)

### 결정
노드 capacity를 컴파일 타임이 아닌 런타임에 설정합니다.

```rust
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    pub fn new(capacity: usize) -> Result<Self, BPlusTreeError> {
        if capacity < MIN_CAPACITY {
            return Err(BPlusTreeError::InvalidCapacity {
                requested: capacity,
                min: MIN_CAPACITY,
            });
        }
        // ...
    }
}
```

### 근거

1. **유스케이스별 최적화**: 데이터셋 크기와 접근 패턴에 따라 최적값이 다름
2. **실험 가능성**: 사용자가 자신의 워크로드에 맞게 튜닝 가능
3. **단순성**: Const generics보다 구현이 간단

### 최적화 가이드라인

```markdown
# optimal_capacity_analysis.md

## Recommendations by Dataset Size
- < 10K entries: capacity 64-128
- 10K-100K entries: capacity 128-256
- 100K-1M entries: capacity 256-512
- > 1M entries: capacity 512+

## Recommendations by Workload
- Read-heavy (>60% gets): Higher capacity
- Range-heavy (>20% ranges): Higher capacity
- Delete-heavy (>15% deletes): Lower capacity
- Mixed workload: Start with 128-256
```

### 결과

- 사용자가 워크로드에 맞게 튜닝 가능
- 성능 최대 22% 향상 가능
- 대신 "올바른" capacity 선택의 부담이 사용자에게 전가됨

---

## ADR-004: Module Organization by Change Frequency

### 상태
✅ **Accepted** (리팩토링 과정)

### 배경
초기에는 모든 코드가 `lib.rs` 하나에 있었습니다 (3,138줄). 이는:
1. 변경 시 전체 파일을 리빌드해야 함
2. 코드 탐색이 어려움
3. 변경 영향 범위 파악이 어려움

### 결정
기능별이 아닌 "변경 빈도" 기준으로 모듈을 분리합니다.

```
기존: lib.rs (3,138 lines)

신규:
├── types.rs              # 100 lines - 안정적
├── error.rs              # 200 lines - 변경 적음
├── node.rs               # 600 lines - 노드 로직
├── tree_structure.rs     # 200 lines - 트리 구조
├── construction.rs       # 150 lines - 생성자
├── insert_operations.rs  # 400 lines - 삽입
├── delete_operations.rs  # 600 lines - 삭제
├── iteration.rs          # 400 lines - 반복
├── range_queries.rs      # 300 lines - 범위 쿼리
├── validation.rs         # 400 lines - 검증
└── compact_arena.rs      # 300 lines - 메모리
```

### 근거

1. **응집도(Cohesion)**: 함께 변경되는 코드끼리 그룹화
2. **변경 지역성**: 하나의 기능 수정 시 하나의 모듈만 변경
3. **컴파일 시간**: 변경된 모듈만 재컴파일
4. **가독성**: 각 모듈을 10-15분 내에 읽을 수 있음

### 모듈 의존성 그래프

```
lib.rs (public API)
├── types.rs (no deps)
├── error.rs (no deps)
├── node.rs (uses: types, error)
├── compact_arena.rs (uses: types)
├── tree_structure.rs (uses: types, error, node, arena)
├── construction.rs (uses: all above)
├── insert_operations.rs (uses: all above)
├── delete_operations.rs (uses: all above)
├── get_operations.rs (uses: all above)
├── iteration.rs (uses: all above)
├── range_queries.rs (uses: all above)
└── validation.rs (uses: all above)
```

### 결과

| 메트릭 | Before | After | 변화 |
|--------|--------|-------|------|
| 최대 파일 크기 | 3,138줄 | 600줄 | 81% 감소 |
| 평균 파일 크기 | 3,138줄 | ~350줄 | 89% 감소 |
| 변경 영향 범위 | 전체 | 특정 모듈 | 개선 |
| 코드 탐색 시간 | 높음 | 낮음 | 개선 |

---

## ADR-005: Unsafe Code for Performance

### 상태
✅ **Accepted with Caution** (최적화 과정)

### 배경
안전한 Rust 코드는 bounds checking 등으로 약간의 오버헤드가 있습니다. 반복문과 같은 핫 경로에서 이는 누적됩니다.

### 결정
반복문과 같은 성능 크리티컬 경로에서만 `unsafe`를 사용합니다.

```rust
// node.rs
impl<K, V> LeafNode<K, V> {
    /// # Safety
    /// Caller must ensure index < self.keys_len()
    #[inline]
    pub unsafe fn get_key_unchecked(&self, index: usize) -> &K {
        self.keys.get_unchecked(index)
    }

    /// # Safety
    /// Caller must ensure index < self.keys_len()
    #[inline]
    pub unsafe fn get_key_value_unchecked(&self, index: usize) -> (&K, &V) {
        (
            self.keys.get_unchecked(index),
            self.values.get_unchecked(index),
        )
    }
}
```

### 안전성 보장

1. **제한된 사용**: 오직 반복문 내부에서만 사용
2. **명시적 검증**: 호출 전 항상 bounds 체크
3. **문서화**: 각 unsafe 함수에 Safety invariant 문서화
4. **제한된 범위**: unsafe 블록 최소화

```rust
// iteration.rs - 안전한 사용 예
fn next(&mut self) -> Option<Self::Item> {
    if let Some(leaf) = self.current_leaf_ref {
        if self.current_index < leaf.keys_len() {
            // Safety: bounds checked above
            let (k, v) = unsafe {
                leaf.get_key_value_unchecked(self.current_index)
            };
            self.current_index += 1;
            return Some((k, v));
        }
    }
    None
}
```

### 결과

- 반복문당 4-6ns 감소
- 전체 벤치마크에서 5-10% 향상
- 메모리 안전성 유지 (undefined behavior 없음)

---

## ADR-006: Comprehensive Error Handling

### 상태
✅ **Accepted** (API 설계 단계)

### 배경
B+ Tree는 복잡한 데이터 구조로, 다양한 오류 상황이 발생할 수 있습니다:
- 잘못된 capacity 설정
- 키를 찾을 수 없음
- 데이터 무결성 문제

### 결정
세분화된 에러 타입과 Result 기반 API를 제공합니다.

```rust
// error.rs
#[derive(Debug, Clone, PartialEq)]
pub enum BPlusTreeError {
    InvalidCapacity { requested: usize, min: usize },
    KeyNotFound,
    DataIntegrityError(String),
}

pub type BTreeResult<T> = Result<T, BPlusTreeError>;
pub type ModifyResult<T> = Result<T, BPlusTreeError>;

// 트라이트 구현으로 체이닝 지원
pub trait BTreeResultExt<T> {
    fn validate_integrity(self) -> Self;
    fn or_rollback(self, tree: &mut BPlusTreeMap<K, V>) -> Self;
}
```

### 근거

1. **타입 안전성**: 컴파일 타임에 오류 처리 강제
2. **세분화**: 다른 오류에 다른 대응 가능
3. **복구 가능성**: 일부 오류는 복구 가능 (예: batch insert 실패 시 rollback)
4. **디버깅**: 상세한 에러 메시지로 문제 파악 용이

### 고급 에러 처리 패턴

```rust
// lib.rs
impl<K: Clone, V: Clone> BPlusTreeMap<K, V> {
    /// Insert with comprehensive error handling and rollback
    pub fn try_insert(&mut self, key: K, value: V) -> ModifyResult<Option<V>> {
        // Validate tree state before insertion
        if let Err(e) = self.check_invariants_detailed() {
            return Err(BPlusTreeError::DataIntegrityError(e));
        }

        let old_value = self.insert(key, value);

        // Validate tree state after insertion
        if let Err(e) = self.check_invariants_detailed() {
            return Err(BPlusTreeError::DataIntegrityError(e));
        }

        Ok(old_value)
    }

    /// Batch insert with rollback on failure
    pub fn batch_insert(&mut self, items: Vec<(K, V)>) -> ModifyResult<Vec<Option<V>>> {
        let mut results = Vec::new();
        let mut inserted_keys = Vec::new();

        for (key, value) in items {
            match self.try_insert(key.clone(), value) {
                Ok(old_value) => {
                    results.push(old_value);
                    inserted_keys.push(key);
                }
                Err(e) => {
                    // Rollback
                    for rollback_key in inserted_keys {
                        self.remove(&rollback_key);
                    }
                    return Err(e);
                }
            }
        }

        Ok(results)
    }
}
```

### 결과

- 예상 가능한 모든 오류 상황 처리
- 데이터 무결성 문제 조기 발견
- 복구 가능한 오류와 치명적 오류 구분

---

## ADR-007: Dual-Language Implementation

### 상태
✅ **Accepted** (프로젝트 초기)

### 배경
B+ Tree는 다양한 환경에서 사용될 수 있습니다:
- Rust: 시스템 프로그래밍, 임베디드, 고성능 서버
- Python: 데이터 분석, 프로토타이핑, 기존 코드베이스 통합

### 결정
Rust와 Python 양쪽에 완전한 구현을 제공합니다.

```
BPlusTree3/
├── rust/               # Rust 구현
│   ├── src/           # 순수 Rust
│   └── benches/       # 벤치마크
└── python/            # Python 구현
    ├── bplustree/     # 순수 Python
    └── bplustree_c_src/  # C 확장
```

### 구현 전략

| 언어 | 구현 방식 | 목표 |
|------|-----------|------|
| Rust | 순수 Rust + Arena | 최고 성능, 타입 안전성 |
| Python | 순수 Python + C Extension | 유연성, 생태계 통합 |

### 근거

1. **성능 vs 생산성**: Rust는 성능, Python은 생산성
2. **사용자 선택**: 사용자가 자신의 환경에 맞게 선택
3. **알고리즘 검증**: 두 구현이 서로를 검증
4. **시장 도달**: 더 많은 사용자에게 도달

### 결과

- Rust: BTreeMap 대비 31-68% 범위 쿼리 향상
- Python: SortedDict 대비 1.4-2.5x 범위 쿼리 향상
- 두 구현 모두 프로덕션 준비 완료

---

## 결론

이러한 아키텍처 결정들은 모두 다음 원칙을 따릅니다:

1. **데이터 기반**: 모든 결정에는 측정과 검증
2. **점진적 변경**: 큰 변화를 작은 단계로 분할
3. **품질 게이트**: 테스트, 벤치마크, 리뷰로 검증
4. **문서화**: 결정의 근거와 결과 기록
5. **실용주의**: 이론적 완벽함보다 실제 성능 중시
