# 테스트 체계 설계 문서

**testing-strategist 에이전트 산출물**  
**팀:** codegen-agent-team  
**작성일:** 2026-02-13

---

## 1. TDD 사이클 정의 (RED → GREEN → REFACTOR)

### 1.1 TDD 사이클 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         TDD 사이클 (10분 단위)                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│    ┌──────────┐      ┌──────────┐      ┌──────────┐      ┌─────────┐   │
│    │   RED    │      │  GREEN   │      │ REFACTOR │      │ COMMIT  │   │
│    │          │      │          │      │          │      │         │   │
│    │  테스트   │ ───→ │  최소한의 │ ───→ │  코드    │ ───→ │  Green  │   │
│    │  작성    │      │  구현    │      │  개선    │      │  완료   │   │
│    │          │      │          │      │          │      │  후     │   │
│    │ (실패    │      │ (테스트  │      │ (테스트  │      │         │   │
│    │  확인)   │      │  통과)   │      │  유지)   │      │         │   │
│    └──────────┘      └──────────┘      └────┬─────┘      └─────────┘   │
│         ↑                                   │                           │
│         └───────────────────────────────────┘                           │
│                      (다음 기능으로)                                     │
│                                                                         │
│   시간 제한: 5-15분 per cycle                                          │
│   커밋 시점: Green phase 완료 후                                        │
│   리팩토링: 테스트 통과 유지하며 코드 품질 개선                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 단계별 상세 정의

#### RED 단계 (테스트 작성)

```rust
// 예시: 새로운 기능에 대한 실패하는 테스트 작성
#[test]
fn test_insert_into_empty_tree() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 행위 검증
    assert_eq!(tree.insert(1, "one"), None);
    assert_eq!(tree.get(&1), Some(&"one"));
    assert_eq!(tree.len(), 1);
}
```

**체크리스트:**
- [ ] 테스트가 명확한 실패 메시지를 제공하는가?
- [ ] 테스트가 하나의 행위만 검증하는가?
- [ ] 테스트 이름이 의도를 명확히 표현하는가?
- [ ] 테스트가 독립적으로 실행 가능한가?

#### GREEN 단계 (최소한의 구현)

```rust
// 최소한의 구현으로 테스트 통과
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    pub fn insert(&mut self, key: K, value: V) -> Option<V> {
        // 최소한의 구현 - 실제 로직은 추후 확장
        match &mut self.root {
            NodeRef::Leaf(leaf_id, _) => {
                let leaf = self.leaf_arena.get_mut(*leaf_id).unwrap();
                leaf.insert(key, value)
            }
            _ => unimplemented!()
        }
    }
}
```

**체크리스트:**
- [ ] 모든 테스트가 통과하는가?
- [ ] 구현이 테스트 요구사항을 만족하는가?
- [ ] 불필요한 복잡성을 추가하지 않았는가?

#### REFACTOR 단계 (코드 개선)

```rust
// 리팩토링: 중복 제거, 가독성 향상
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    pub fn insert(&mut self, key: K, value: V) -> Option<V> {
        let leaf_id = self.root.leaf_id().expect("root is leaf");
        let leaf = self.leaf_arena.get_mut(leaf_id).unwrap();
        
        match leaf.insert(key, value) {
            InsertResult::Updated(old) => old,
            InsertResult::Split { new_node_data, separator_key } => {
                self.handle_root_split(new_node_data, separator_key);
                None
            }
        }
    }
}
```

**체크리스트:**
- [ ] 테스트가 여전히 통과하는가?
- [ ] 코드 가독성이 향상되었는가?
- [ ] 중복이 제거되었는가?
- [ ] 성능이 저하되지 않았는가?

### 1.3 TDD 커밋 메시지 규칙

```
커밋 타이밍: Green phase 완료 후

형식:
test(<scope>): <description>

예시:
test(insert): add test for root split scenario
test(delete): add edge case tests for underflow
test(range): add property-based tests for range queries
```

---

## 2. 테스트 디렉토리 구조 설계

### 2.1 전체 구조

```
tests/
├── README.md                    # 테스트 가이드
├── common/                      # 테스트 공용 코드
│   ├── mod.rs
│   ├── fixtures.rs              # 테스트 픽스처
│   ├── assertions.rs            # 커스텀 assertion
│   └── helpers.rs               # 테스트 헬퍼
│
├── unit/                        # 단위 테스트 (60%)
│   ├── mod.rs
│   ├── test_node.rs             # 노드 단위 테스트
│   ├── test_arena.rs            # 아레나 단위 테스트
│   ├── test_insert.rs           # 삽입 연산 테스트
│   ├── test_delete.rs           # 삭제 연산 테스트
│   ├── test_query.rs            # 조회 연산 테스트
│   └── test_iteration.rs        # 순회 테스트
│
├── integration/                 # 통합 테스트 (30%)
│   ├── mod.rs
│   ├── test_construction.rs     # 생성자 통합 테스트
│   ├── test_operations.rs       # 연산 통합 테스트
│   ├── test_range.rs            # 범위 쿼리 통합 테스트
│   ├── test_api_compatibility.rs # API 호환성 테스트
│   └── test_error_handling.rs   # 에러 처리 테스트
│
├── adversarial/                 # 적대적 테스트 (10%)
│   ├── mod.rs
│   ├── test_edge_cases.rs       # 경계 조건 테스트
│   ├── test_stress.rs           # 스트레스 테스트
│   ├── test_fuzz.rs             # 퍼즈 테스트
│   ├── test_invariants.rs       # 불변식 검증 테스트
│   └── test_memory_safety.rs    # 메모리 안전성 테스트
│
└── fixtures/                    # 테스트 픽스처
    ├── small_tree.json          # 작은 트리 데이터
    ├── medium_tree.json         # 중간 트리 데이터
    └── large_tree.bin           # 대용량 트리 데이터
```

### 2.2 테스트 비율 및 목표

| 카테고리 | 비율 | 목표 커버리지 | 목적 |
|----------|------|---------------|------|
| **Unit** | 60% | 80% | 개별 컴포넌트 검증 |
| **Integration** | 30% | 70% | 시나리오 및 상호작용 검증 |
| **Adversarial** | 10% | 핵심 경로 100% | 엣지 케이스 및 견고성 검증 |
| **Total** | 100% | 85%+ | 전체 신뢰성 확보 |

### 2.3 단위 테스트 (tests/unit/)

```rust
// tests/unit/test_node.rs
use crate::common::fixtures::create_test_leaf;

#[test]
fn test_leaf_node_insert_basic() {
    let mut leaf = create_test_leaf(4);
    
    assert!(leaf.insert(1, "a").is_none());
    assert!(leaf.insert(2, "b").is_none());
    
    assert_eq!(leaf.len(), 2);
    assert_eq!(leaf.get(&1), Some(&"a"));
}

#[test]
fn test_leaf_node_insert_full() {
    let mut leaf = create_test_leaf(2);
    
    leaf.insert(1, "a");
    leaf.insert(2, "b");
    
    // Split 발생
    let result = leaf.insert(3, "c");
    assert!(matches!(result, InsertResult::Split { .. }));
}

#[test]
fn test_branch_node_navigation() {
    let branch = create_test_branch(vec![10, 20], vec![0, 1, 2]);
    
    assert_eq!(branch.find_child(&5), 0);   // 5 < 10
    assert_eq!(branch.find_child(&10), 1);  // 10 >= 10, 10 < 20
    assert_eq!(branch.find_child(&25), 2);  // 25 >= 20
}
```

### 2.4 통합 테스트 (tests/integration/)

```rust
// tests/integration/test_operations.rs
use bplustree::BPlusTreeMap;

#[test]
fn test_insert_delete_sequence() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 삽입 시퀀스
    for i in 0..100 {
        tree.insert(i, i * 2);
    }
    assert_eq!(tree.len(), 100);
    
    // 삭제 시퀀스
    for i in 0..100 {
        assert_eq!(tree.remove(&i), Some(i * 2));
    }
    assert!(tree.is_empty());
}

#[test]
fn test_range_operations() {
    let mut tree = BPlusTreeMap::new(16).unwrap();
    
    for i in 0..1000 {
        tree.insert(i, i.to_string());
    }
    
    // 범위 쿼리
    let range: Vec<_> = tree.range(100..200).collect();
    assert_eq!(range.len(), 100);
    
    // Inclusive range
    let range: Vec<_> = tree.range(100..=200).collect();
    assert_eq!(range.len(), 101);
}
```

---

## 3. 적대적 테스트 시나리오 예시

### 3.1 적대적 테스트 전략

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      적대적 테스트 전략                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   1. 경계 조건 공격 (Boundary Attacks)                                  │
│      ├── 최소/최대 키값                                                 │
│      ├── 빈 트리/단일 노드                                              │
│      └── 최대 용량 근접                                                 │
│                                                                         │
│   2. 순서 공격 (Ordering Attacks)                                       │
│      ├── 정순/역순 삽입                                                 │
│      ├── 교대 삽입 (min/max 번갈아)                                     │
│      └── 무작위 삭제 패턴                                               │
│                                                                         │
│   3. 구조 공격 (Structural Attacks)                                     │
│      ├── 극단적 불균형 유도                                             │
│      ├── 연속적인 split/merge                                           │
│      └── 깊은 트리 생성                                                 │
│                                                                         │
│   4. 자원 공격 (Resource Attacks)                                       │
│      ├── 대용량 데이터                                                  │
│      ├── 빠른 할당/해제                                                 │
│      └── 메모리 압력                                                    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 적대적 테스트 예시 코드

#### 경계 조건 공격

```rust
// tests/adversarial/test_edge_cases.rs

#[test]
fn test_empty_tree_operations() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 빈 트리에서의 연산
    assert_eq!(tree.get(&1), None);
    assert_eq!(tree.remove(&1), None);
    assert_eq!(tree.len(), 0);
    assert!(tree.is_empty());
    assert_eq!(tree.iter().count(), 0);
    assert_eq!(tree.range(0..10).count(), 0);
}

#[test]
fn test_single_element_tree() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    tree.insert(42, "answer");
    
    // 단일 요소 연산
    assert_eq!(tree.get(&42), Some(&"answer"));
    assert_eq!(tree.get(&0), None);
    
    // 순회
    let items: Vec<_> = tree.iter().collect();
    assert_eq!(items, vec![(&42, &"answer")]);
    
    // 삭제 후 빈 트리
    assert_eq!(tree.remove(&42), Some("answer"));
    assert!(tree.is_empty());
}

#[test]
fn test_minimum_capacity() {
    // 최소 용량 (2) 테스트
    let mut tree = BPlusTreeMap::new(2).unwrap();
    
    for i in 0..10 {
        tree.insert(i, i);
        tree.check_invariants().unwrap();
    }
    
    for i in 0..10 {
        tree.remove(&i);
        tree.check_invariants().unwrap();
    }
}

#[test]
fn test_maximum_key_values() {
    let mut tree = BPlusTreeMap::new(16).unwrap();
    
    // 극단적 키값
    tree.insert(i32::MIN, "min");
    tree.insert(i32::MAX, "max");
    tree.insert(0, "zero");
    
    assert_eq!(tree.get(&i32::MIN), Some(&"min"));
    assert_eq!(tree.get(&i32::MAX), Some(&"max"));
    
    // 범위 쿼리
    let range: Vec<_> = tree.range(i32::MIN..=i32::MAX).collect();
    assert_eq!(range.len(), 3);
}
```

#### 순서 공격

```rust
// tests/adversarial/test_edge_cases.rs

#[test]
fn test_ascending_insert() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 오름차순 삽입 - 오른쪽 치우침 유도
    for i in 0..1000 {
        tree.insert(i, i);
    }
    
    tree.check_invariants().unwrap();
    assert_eq!(tree.len(), 1000);
}

#[test]
fn test_descending_insert() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 내림차순 삽입 - 왼쪽 치우침 유도
    for i in (0..1000).rev() {
        tree.insert(i, i);
    }
    
    tree.check_invariants().unwrap();
    assert_eq!(tree.len(), 1000);
}

#[test]
fn test_alternating_min_max() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 최소/최대 번갈아 삽입
    for round in 0..100 {
        tree.insert(round as i32 * -1000, round);
        tree.insert(round as i32 * 1000, round);
        tree.check_invariants().unwrap();
    }
    
    assert_eq!(tree.len(), 200);
}

#[test]
fn test_random_delete_pattern() {
    use rand::seq::SliceRandom;
    
    let mut tree = BPlusTreeMap::new(4).unwrap();
    let mut keys: Vec<i32> = (0..1000).collect();
    
    // 삽입
    for &key in &keys {
        tree.insert(key, key * 2);
    }
    
    // 무작위 삭제
    keys.shuffle(&mut rand::thread_rng());
    for key in keys {
        tree.remove(&key);
        tree.check_invariants().unwrap();
    }
    
    assert!(tree.is_empty());
}
```

#### 구조 공격

```rust
// tests/adversarial/test_stress.rs

#[test]
fn test_rapid_split_merge_cycles() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // Split/Merge 사이클 반복
    for cycle in 0..50 {
        // 삽입으로 split 유도
        for i in 0..100 {
            tree.insert(cycle * 1000 + i, i);
        }
        
        // 삭제로 merge 유도
        for i in 0..100 {
            tree.remove(&(cycle * 1000 + i));
        }
        
        tree.check_invariants().unwrap();
        assert!(tree.is_empty());
    }
}

#[test]
fn test_deep_tree_creation() {
    let mut tree = BPlusTreeMap::new(2).unwrap();
    
    // 깊은 트리 생성 (작은 capacity로)
    for i in 0..10000 {
        tree.insert(i, i);
    }
    
    // 깊은 트리에서의 연산
    assert_eq!(tree.get(&5000), Some(&5000));
    assert_eq!(tree.get(&9999), Some(&9999));
    
    // 전체 순회
    let count = tree.iter().count();
    assert_eq!(count, 10000);
}

#[test]
fn test_linked_list_integrity() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 데이터 삽입
    for i in 0..1000 {
        tree.insert(i, i * 2);
    }
    
    // 무작위 삭제 후 연결 리스트 일관성 검증
    let mut remaining: Vec<_> = (0..1000).collect();
    remaining.shuffle(&mut rand::thread_rng());
    
    for key in remaining.iter().take(500) {
        tree.remove(key);
    }
    
    // 연결 리스트 순회로 순서 검증
    let mut prev = -1i32;
    for (key, _) in tree.iter() {
        assert!(*key > prev, "Linked list order violated at key {}", key);
        prev = *key;
    }
}
```

#### 자원 공격

```rust
// tests/adversarial/test_memory_safety.rs

#[test]
fn test_large_dataset() {
    let mut tree = BPlusTreeMap::new(128).unwrap();
    
    // 대용량 데이터
    for i in 0..1_000_000 {
        tree.insert(i, i.to_string());
    }
    
    assert_eq!(tree.len(), 1_000_000);
    
    // 성능 검증
    let start = std::time::Instant::now();
    for i in 0..1000 {
        assert_eq!(tree.get(&i), Some(&i.to_string()));
    }
    let elapsed = start.elapsed();
    assert!(elapsed < std::time::Duration::from_millis(10));
}

#[test]
fn test_rapid_allocation_deallocation() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 빠른 할당/해제 반복
    for iteration in 0..100 {
        for i in 0..100 {
            tree.insert(i + iteration * 1000, i);
        }
        
        for i in 0..100 {
            tree.remove(&(i + iteration * 1000));
        }
        
        // 아레나 상태 검증
        let stats = tree.arena_stats();
        assert_eq!(stats.allocated_nodes, 1); // root만 남음
    }
}

#[test]
fn test_arena_reuse() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 초기 상태
    let initial_stats = tree.arena_stats();
    
    // 할당/해제 반복
    for _ in 0..10 {
        for i in 0..1000 {
            tree.insert(i, i);
        }
        tree.clear();
    }
    
    // 아레나 재사용 확인
    let final_stats = tree.arena_stats();
    assert!(final_stats.total_allocations < 100); // 재사용이 일어났는지 확인
}
```

### 3.3 불변식 검증 테스트

```rust
// tests/adversarial/test_invariants.rs

#[test]
fn test_btree_invariants() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    // 무작위 연산 후 불변식 검증
    let mut rng = rand::thread_rng();
    
    for _ in 0..1000 {
        let op: u8 = rng.gen_range(0..3);
        let key: i32 = rng.gen_range(0..100);
        
        match op {
            0 => { tree.insert(key, key); }
            1 => { tree.remove(&key); }
            2 => { tree.get(&key); }
            _ => unreachable!()
        }
        
        // 매 연산 후 불변식 검증
        tree.check_invariants().unwrap();
    }
}

#[test]
fn test_node_capacity_invariant() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    
    for i in 0..100 {
        tree.insert(i, i);
        
        // 모든 노드가 용량 제한을 준수하는지 확인
        for node in tree.iter_nodes() {
            assert!(node.len() <= 4, "Node exceeds capacity");
            if !node.is_root() {
                assert!(node.len() >= 2, "Non-root node under minimum");
            }
        }
    }
}

#[test]
fn test_sorted_order_invariant() {
    let mut tree = BPlusTreeMap::new(4).unwrap();
    let mut rng = rand::thread_rng();
    
    // 무작위 삽입
    for _ in 0..1000 {
        let key: i32 = rng.gen();
        tree.insert(key, key);
    }
    
    // 정렬 순서 검증
    let keys: Vec<_> = tree.keys().copied().collect();
    assert!(is_sorted(&keys), "Keys must be sorted");
}

fn is_sorted<T: Ord>(slice: &[T]) -> bool {
    slice.windows(2).all(|w| w[0] <= w[1])
}
```

---

## 4. 성능 벤치마크 전략 및 benches/ 구조 설계

### 4.1 벤치마크 디렉토리 구조

```
benches/
├── README.md                    # 벤치마크 가이드
├── criterion/                   # Criterion 벤치마크
│   ├── mod.rs
│   ├── bench_insert.rs          # 삽입 성능
│   ├── bench_delete.rs          # 삭제 성능
│   ├── bench_query.rs           # 조회 성능
│   ├── bench_iteration.rs       # 순회 성능
│   ├── bench_range.rs           # 범위 쿼리 성능
│   └── bench_comparison.rs      # BTreeMap과 비교
│
├── iai/                         # IAI 정적 벤치마크 (선택적)
│   └── bench_static.rs
│
├── fixtures/                    # 벤치마크 데이터
│   ├── small_dataset.rs
│   ├── medium_dataset.rs
│   └── large_dataset.rs
│
└── scripts/                     # 벤치마크 스크립트
    ├── run_all.sh               # 전체 벤치마크 실행
    ├── compare.sh               # 성능 비교
    └── profile.sh               # 프로파일링
```

### 4.2 Criterion 벤치마크 설정

```rust
// benches/criterion/bench_insert.rs
use criterion::{black_box, criterion_group, criterion_main, Criterion, BenchmarkId};
use bplustree::BPlusTreeMap;
use std::collections::BTreeMap;

fn bench_insert_sequential(c: &mut Criterion) {
    let mut group = c.benchmark_group("insert_sequential");
    
    for size in [100, 1_000, 10_000, 100_000] {
        // BPlusTree
        group.bench_with_input(
            BenchmarkId::new("bplustree", size),
            &size,
            |b, &size| {
                b.iter(|| {
                    let mut tree = BPlusTreeMap::new(128).unwrap();
                    for i in 0..size {
                        tree.insert(i, i * 2);
                    }
                    black_box(tree);
                });
            },
        );
        
        // BTreeMap 비교
        group.bench_with_input(
            BenchmarkId::new("btreemap", size),
            &size,
            |b, &size| {
                b.iter(|| {
                    let mut tree = BTreeMap::new();
                    for i in 0..size {
                        tree.insert(i, i * 2);
                    }
                    black_box(tree);
                });
            },
        );
    }
    
    group.finish();
}

fn bench_insert_random(c: &mut Criterion) {
    use rand::seq::SliceRandom;
    
    let mut group = c.benchmark_group("insert_random");
    
    for size in [100, 1_000, 10_000] {
        let mut keys: Vec<i32> = (0..size as i32).collect();
        keys.shuffle(&mut rand::thread_rng());
        
        group.bench_with_input(
            BenchmarkId::new("bplustree", size),
            &keys,
            |b, keys| {
                b.iter(|| {
                    let mut tree = BPlusTreeMap::new(128).unwrap();
                    for &key in keys {
                        tree.insert(key, key * 2);
                    }
                    black_box(tree);
                });
            },
        );
    }
    
    group.finish();
}

criterion_group!(insert_benches, bench_insert_sequential, bench_insert_random);
criterion_main!(insert_benches);
```

```rust
// benches/criterion/bench_range.rs
use criterion::{black_box, criterion_group, criterion_main, Criterion, BenchmarkId};
use bplustree::BPlusTreeMap;
use std::collections::BTreeMap;

fn bench_range_query(c: &mut Criterion) {
    let mut group = c.benchmark_group("range_query");
    
    for size in [1_000, 10_000, 100_000] {
        // BPlusTree 준비
        let mut bplus_tree = BPlusTreeMap::new(128).unwrap();
        for i in 0..size {
            bplus_tree.insert(i, i * 2);
        }
        
        // BTreeMap 준비
        let mut btree = BTreeMap::new();
        for i in 0..size {
            btree.insert(i, i * 2);
        }
        
        let range_size = size / 10;
        
        // BPlusTree 벤치마크
        group.bench_with_input(
            BenchmarkId::new("bplustree", size),
            &range_size,
            |b, &range_size| {
                b.iter(|| {
                    let sum: usize = bplus_tree.range(0..range_size)
                        .map(|(k, v)| k + v)
                        .sum();
                    black_box(sum);
                });
            },
        );
        
        // BTreeMap 벤치마크
        group.bench_with_input(
            BenchmarkId::new("btreemap", size),
            &range_size,
            |b, &range_size| {
                b.iter(|| {
                    let sum: usize = btree.range(0..range_size)
                        .map(|(k, v)| k + v)
                        .sum();
                    black_box(sum);
                });
            },
        );
    }
    
    group.finish();
}

criterion_group!(range_benches, bench_range_query);
criterion_main!(range_benches);
```

### 4.3 벤치마크 스크립트

```bash
#!/bin/bash
# benches/scripts/run_all.sh

echo "=== Running All Benchmarks ==="

# Criterion 벤치마크
echo "Running Criterion benchmarks..."
cargo bench --bench insert
cargo bench --bench delete
cargo bench --bench query
cargo bench --bench iteration
cargo bench --bench range

# 결과 요약
echo ""
echo "=== Benchmark Summary ==="
cat target/criterion/report/index.html | grep -A 5 "summary"
```

```bash
#!/bin/bash
# benches/scripts/compare.sh

# 기준 브랜치와 비교
BASELINE_BRANCH=${1:-main}
CURRENT_BRANCH=$(git branch --show-current)

echo "Comparing $CURRENT_BRANCH with $BASELINE_BRANCH..."

# 기준 브랜치에서 벤치마크 실행
git checkout $BASELINE_BRANCH
cargo bench -- --save-baseline baseline

# 현재 브랜치에서 벤치마크 실행
git checkout $CURRENT_BRANCH
cargo bench -- --baseline baseline

echo "Comparison complete. Check target/criterion/report for details."
```

```bash
#!/bin/bash
# benches/scripts/profile.sh

# Release 빌드 with debug symbols
RUSTFLAGS="-g" cargo build --release --example benchmark

# Instruments 실행 (macOS)
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "Running Instruments profiler..."
    xcrun instruments -t "Time Profiler" \
        -D "profile.trace" \
        target/release/examples/benchmark
    echo "Profile saved to profile.trace"
else
    echo "Using perf for profiling..."
    perf record --call-graph dwarf target/release/examples/benchmark
    perf report
fi
```

### 4.4 성능 회귀 감지

```yaml
# .github/workflows/benchmark.yml
name: Performance Regression Check

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  benchmark:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Run benchmarks
        run: |
          cargo bench -- --save-baseline new

      - name: Compare with main
        run: |
          git fetch origin main
          git checkout origin/main
          cargo bench -- --save-baseline main
          cargo bench -- --baseline main

      - name: Check regression
        run: |
          # 10% 이상 성능 저하 시 실패
          cargo bench -- --baseline main --regression-threshold 10
```

---

## 5. Tidy First 워크플로우 문서화

### 5.1 Tidy First 원칙

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Tidy First 원칙                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   "구조적 변경"과 "행위적 변경"을 분리하라                              │
│                                                                         │
│   구조적 변경 (Structural Changes):                                     │
│   - 파일/모듈 이동                                                      │
│   - 함수/변수 이름 변경                                                 │
│   - 코드 재배치                                                         │
│   - 중복 제거                                                           │
│   - 가독성 개선                                                         │
│                                                                         │
│   행위적 변경 (Behavioral Changes):                                     │
│   - 새로운 기능 추가                                                    │
│   - 버그 수정                                                           │
│   - 알고리즘 변경                                                       │
│   - 성능 최적화                                                         │
│                                                                         │
│   규칙: 항상 구조적 변경을 먼저, 행위적 변경은 나중에                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.2 Tidy First 워크플로우

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Tidy First 워크플로우                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 1. IDENTIFY                                                      │  │
│   │    - 코드 스멜 식별                                              │  │
│   │    - 개선 영역 결정                                              │  │
│   │    - 변경 범위 정의                                              │  │
│   └──────────────────────────────┬──────────────────────────────────┘  │
│                                  ↓                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 2. STRUCTURAL CHANGE                                             │  │
│   │    - 코드 재조직                                                 │  │
│   │    - 이름 개선                                                   │  │
│   │    - 중복 제거                                                   │  │
│   │    - 테스트 실행 (모두 통과해야 함)                              │  │
│   └──────────────────────────────┬──────────────────────────────────┘  │
│                                  ↓                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 3. COMMIT STRUCTURAL                                             │  │
│   │    git commit -m "structural: <description>"                     │  │
│   │    예: "structural: extract validation module"                   │  │
│   └──────────────────────────────┬──────────────────────────────────┘  │
│                                  ↓                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 4. BEHAVIORAL CHANGE                                             │  │
│   │    - 새로운 기능 구현                                            │  │
│   │    - TDD 사이클 적용                                             │  │
│   │    - 테스트 작성 및 통과                                         │  │
│   └──────────────────────────────┬──────────────────────────────────┘  │
│                                  ↓                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 5. COMMIT BEHAVIORAL                                             │  │
│   │    git commit -m "behavioral: <description>"                     │  │
│   │    예: "behavioral: add batch insert API"                        │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.3 커밋 메시지 규칙

```
구조적 변경:
structural: <변경 내용>

예시:
structural: extract error module from lib.rs
structural: rename NodeRef variants for clarity
structural: reorganize operations module structure
structural: consolidate duplicate validation logic

행위적 변경:
behavioral: <변경 내용>

예시:
behavioral: add range query with custom bounds
behavioral: implement batch insert operation
behavioral: optimize leaf node search with binary search
behavioral: fix edge case in node rebalancing
```

### 5.4 Tidy First 적용 예시

#### 예시 1: 모듈 분리

```bash
# BEFORE: lib.rs에 모든 코드가 있음 (3138줄)

# 1. IDENTIFY
# - lib.rs가 너무 큼
# - error/types를 별도 모듈로 분리 필요

# 2. STRUCTURAL CHANGE
# error.rs 생성
cat > src/error.rs << 'EOF'
use std::fmt;

#[derive(Debug, Clone, PartialEq)]
pub enum BPlusTreeError {
    InvalidCapacity { requested: usize, min: usize },
    KeyNotFound,
    DataIntegrityError(String),
}

impl fmt::Display for BPlusTreeError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            Self::InvalidCapacity { requested, min } => {
                write!(f, "Invalid capacity: {} (min: {})", requested, min)
            }
            Self::KeyNotFound => write!(f, "Key not found"),
            Self::DataIntegrityError(msg) => {
                write!(f, "Data integrity error: {}", msg)
            }
        }
    }
}

impl std::error::Error for BPlusTreeError {}

pub type BTreeResult<T> = Result<T, BPlusTreeError>;
EOF

# lib.rs 업데이트 (import만 변경, 로직 변경 없음)
# pub mod error;
# pub use error::{BPlusTreeError, BTreeResult};

# 3. 테스트 실행
cargo test
# test result: ok. 75 passed

# 4. STRUCTURAL COMMIT
git add src/error.rs src/lib.rs
git commit -m "structural: extract error module from lib.rs

- Move BPlusTreeError to dedicated error.rs
- Update imports in lib.rs
- No behavioral changes
- All 75 tests pass"

# 5. 이제 행위적 변경 가능 (새로운 에러 타입 추가 등)
```

#### 예시 2: 함수 분리

```rust
// BEFORE: 하나의 큰 함수
fn insert_internal(&mut self, key: K, value: V) -> Option<V> {
    // 100+ 줄의 복잡한 로직
    // split 처리
    // rebalance 처리
    // ...
}

// AFTER: Tidy First 적용

// 1. STRUCTURAL CHANGE: 함수 분리
fn insert_internal(&mut self, key: K, value: V) -> Option<V> {
    let leaf_id = self.find_leaf(&key);
    let result = self.insert_into_leaf(leaf_id, key, value);
    
    match result {
        InsertResult::Updated(old) => old,
        InsertResult::Split(data) => self.handle_split(data),
    }
}

fn insert_into_leaf(&mut self, leaf_id: NodeId, key: K, value: V) -> InsertResult<K, V> {
    // leaf 삽입 로직
}

fn handle_split(&mut self, data: SplitData<K, V>) -> Option<V> {
    // split 처리 로직
}

// 2. STRUCTURAL COMMIT
// git commit -m "structural: split insert_internal into smaller functions"

// 3. 이제 행위적 변경 가능 (insert 로직 개선 등)
```

### 5.5 Tidy First 체크리스트

#### 구조적 변경 전 체크리스트

- [ ] 변경이 순수하게 구조적임을 확인했는가?
- [ ] 행위 변경이 없음을 테스트로 검증했는가?
- [ ] 커밋 메시지에 "structural:" 접두사를 사용했는가?
- [ ] 변경 이유를 커밋 본문에 설명했는가?

#### 행위적 변경 전 체크리스트

- [ ] 이전 구조적 변경이 완료되었는가?
- [ ] TDD 사이클을 따랐는가?
- [ ] 새로운 테스트를 작성했는가?
- [ ] 커밋 메시지에 "behavioral:" 접두사를 사용했는가?

---

## 6. 통합 테스트 워크플로우

### 6.1 전체 개발 사이클

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      통합 개발 사이클                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 1. DISCOVERY                                                     │  │
│   │    - 새로운 기능/버그 식별                                       │  │
│   │    - 요구사항 정의                                               │  │
│   └──────────────────────────────┬──────────────────────────────────┘  │
│                                  ↓                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 2. TIDY FIRST (필요시)                                           │  │
│   │    - 구조적 변경                                                 │  │
│   │    - structural 커밋                                             │  │
│   └──────────────────────────────┬──────────────────────────────────┘  │
│                                  ↓                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 3. TDD CYCLE                                                     │  │
│   │    ┌─────┐    ┌──────┐    ┌─────────┐                          │  │
│   │    │ RED │───→│ GREEN│───→│ REFACTOR│                          │  │
│   │    └─────┘    └──────┘    └────┬────┘                          │  │
│   │         ↑───────────────────────┘                                │  │
│   │    (반복)                                                        │  │
│   └──────────────────────────────┬──────────────────────────────────┘  │
│                                  ↓                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 4. BEHAVIORAL COMMIT                                             │  │
│   │    git commit -m "behavioral: <description>"                     │  │
│   └──────────────────────────────┬──────────────────────────────────┘  │
│                                  ↓                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 5. VERIFICATION                                                  │  │
│   │    - 전체 테스트 스위트 실행                                     │  │
│   │    - 벤치마크로 성능 확인                                        │  │
│   │    - 적대적 테스트로 견고성 검증                                 │  │
│   │    - 커버리지 리포트 확인                                        │  │
│   └──────────────────────────────┬──────────────────────────────────┘  │
│                                  ↓                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ 6. INTEGRATION                                                   │  │
│   │    - PR 생성                                                     │  │
│   │    - 코드 리뷰                                                   │  │
│   │    - CI/CD 검증                                                  │  │
│   │    - 머지                                                        │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.2 품질 게이트

| 단계 | 검증 항목 | 통과 기준 |
|------|-----------|-----------|
| **Unit Test** | 단위 테스트 | 100% 통과 |
| **Integration Test** | 통합 테스트 | 100% 통과 |
| **Adversarial Test** | 적대적 테스트 | 100% 통과 |
| **Coverage** | 코드 커버리지 | 80% 이상 |
| **Benchmark** | 성능 벤치마크 | 기준 대비 10% 이내 |
| **Lint** | 정적 분석 | Warning 없음 |
| **Format** | 코드 포맷팅 | rustfmt 통과 |

---

## 7. 참고 문서

- [Code Structure Design](code-structure-design.md)
- [Development Feedback Loops](development-feedback-loops.md)
- [Architecture Decisions](architecture-decisions.md)

---

**문서 버전:** 1.0  
**작성자:** testing-strategist 에이전트  
**검토자:** (team-lead 검토 대기)
