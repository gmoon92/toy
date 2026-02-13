# 코드 구조 설계 문서

**code-designer 에이전트 산출물**  
**팀:** codegen-agent-team  
**작성일:** 2026-02-13

---

## 1. 통합 디렉토리 구조

```
project-root/
├── Cargo.toml              # 프로젝트 설정
├── Cargo.lock              # 의존성 잠금
├── README.md               # 프로젝트 개요
├── CHANGELOG.md            # 변경 이력
├── LICENSE                 # 라이선스
│
├── src/                    # 소스 코드
│   ├── lib.rs              # 공개 API 및 모듈 선언 (~200줄)
│   ├── types.rs            # 핵심 타입 정의 (~100줄, 안정적)
│   ├── error.rs            # 에러 처리 (~200줄)
│   │
│   ├── core/               # 핵심 모듈
│   │   ├── mod.rs          # core 모듈 선언
│   │   ├── node.rs         # 노드 구현 (~600줄)
│   │   ├── arena.rs        # 아레나 메모리 관리 (~300줄)
│   │   └── tree.rs         # 트리 구조 관리 (~200줄)
│   │
│   ├── operations/         # 연산 모듈
│   │   ├── mod.rs          # operations 모듈 선언
│   │   ├── construction.rs # 생성자 구현 (~150줄)
│   │   ├── insert.rs       # 삽입 연산 (~400줄)
│   │   ├── delete.rs       # 삭제 연산 (~600줄)
│   │   └── query.rs        # 조회 연산 (~200줄)
│   │
│   ├── traversal/          # 순회 모듈
│   │   ├── mod.rs          # traversal 모듈 선언
│   │   ├── iteration.rs    # 이터레이터 구현 (~400줄)
│   │   └── range.rs        # 범위 쿼리 (~300줄)
│   │
│   ├── utils/              # 유틸리티
│   │   ├── mod.rs          # utils 모듈 선언
│   │   ├── macros.rs       # 매크로 정의
│   │   └── helpers.rs      # 헬퍼 함수
│   │
│   └── validation/         # 검증 모듈
│       ├── mod.rs          # validation 모듈 선언
│       └── invariants.rs   # 불변식 검증 (~400줄)
│
├── tests/                  # 테스트 코드
│   ├── integration/        # 통합 테스트
│   │   ├── mod.rs
│   │   ├── test_insert.rs
│   │   ├── test_delete.rs
│   │   ├── test_query.rs
│   │   └── test_range.rs
│   ├── property/           # 속성 기반 테스트
│   │   └── proptest.rs
│   └── adversarial/        # 적대적 테스트
│       └── fuzz_tests.rs
│
├── benches/                # 벤치마크
│   ├── criterion/          # Criterion 벤치마크
│   │   ├── bench_insert.rs
│   │   ├── bench_delete.rs
│   │   ├── bench_iteration.rs
│   │   └── bench_range.rs
│   └── iai/                # IAI 정적 벤치마크
│
├── docs/                   # 문서
│   ├── architecture/       # 아키텍처 문서
│   │   ├── decisions/      # ADRs
│   │   ├── diagrams/       # 다이어그램
│   │   └── patterns.md     # 설계 패턴
│   ├── api/                # API 문서
│   └── examples/           # 사용 예시
│
├── scripts/                # 개발 스크립트
│   ├── build.sh            # 빌드 스크립트
│   ├── test.sh             # 테스트 실행
│   ├── bench.sh            # 벤치마크 실행
│   └── coverage.sh         # 커버리지 측정
│
├── examples/               # 예제 코드
│   ├── basic_usage.rs      # 기본 사용법
│   ├── advanced_api.rs     # 고급 API
│   └── performance.rs      # 성능 튜닝
│
└── .github/                # GitHub 설정
    ├── workflows/          # CI/CD 워크플로우
    └── ISSUE_TEMPLATE/     # 이슈 템플릿
```

---

## 2. 파일별 책임 및 크기 제한

### 2.1 핵심 파일 (src/)

| 파일 | 책임 | 크기 제한 | 변경 빈도 | 의존성 |
|------|------|-----------|-----------|--------|
| `lib.rs` | 공개 API 선언, 모듈 re-export, crate 문서화 | ~200줄 | 낮음 | 없음 (최상위) |
| `types.rs` | 핵심 타입 정의, 상수, 공용 구조체 | ~100줄 | 매우 낮음 (안정적) | 없음 |
| `error.rs` | 에러 타입, Result 별칭, 에러 확장 트레이트 | ~200줄 | 낮음 | `types.rs` |

### 2.2 Core 모듈 (src/core/)

| 파일 | 책임 | 크기 제한 | 변경 빈도 | 의존성 |
|------|------|-----------|-----------|--------|
| `node.rs` | LeafNode/BranchNode 구현, 노드 수준 연산 | ~600줄 | 중간 | `types.rs`, `error.rs` |
| `arena.rs` | CompactArena 메모리 관리 | ~300줄 | 낮음 | `types.rs` |
| `tree.rs` | 트리 구조 관리, 낶비게이션 헬퍼 | ~200줄 | 낮음 | `types.rs`, `node.rs`, `arena.rs` |

### 2.3 Operations 모듈 (src/operations/)

| 파일 | 책임 | 크기 제한 | 변경 빈도 | 의존성 |
|------|------|-----------|-----------|--------|
| `construction.rs` | 생성자, 초기화 로직 | ~150줄 | 매우 낮음 | `core::*` |
| `insert.rs` | 삽입 연산, 노드 분할 | ~400줄 | 중간 | `core::*` |
| `delete.rs` | 삭제 연산, 재조정, 병합 | ~600줄 | 중간 | `core::*` |
| `query.rs` | 조회 연산, 키 검색 | ~200줄 | 낮음 | `core::tree` |

### 2.4 Traversal 모듈 (src/traversal/)

| 파일 | 책임 | 크기 제한 | 변경 빈도 | 의존성 |
|------|------|-----------|-----------|--------|
| `iteration.rs` | 이터레이터 구현 (Item, Key, Value) | ~400줄 | 중간 | `core::node` |
| `range.rs` | 범위 쿼리, RangeIterator | ~300줄 | 낮음 | `traversal::iteration` |

### 2.5 Utils 모듈 (src/utils/)

| 파일 | 책임 | 크기 제한 | 변경 빈도 | 의존성 |
|------|------|-----------|-----------|--------|
| `macros.rs` | 낶부 매크로 정의 | ~100줄 | 매우 낮음 | 없음 |
| `helpers.rs` | 공용 헬퍼 함수 | ~150줄 | 낮음 | `types.rs` |

### 2.6 Validation 모듈 (src/validation/)

| 파일 | 책임 | 크기 제한 | 변경 빈도 | 의존성 |
|------|------|-----------|-----------|--------|
| `invariants.rs` | 불변식 검증, 디버깅 유틸리티 | ~400줄 | 중간 | 모든 모듈 |

---

## 3. 의존성 그래프 및 규칙

### 3.1 모듈 의존성 그래프

```
lib.rs (public API)
│
├── types.rs ◄──────────────────────────────────────┐
│                                                    │
├── error.rs ◄─────────────────────────────────────┐│
│                                                   ││
├── core/                                           ││
│   ├── mod.rs                                      ││
│   ├── arena.rs ◄─────────────────────────────────┐││
│   │   └── types.rs ◄─────────────────────────────┘││
│   ├── node.rs ◄─────────────────────────────────┐ ││
│   │   ├── types.rs ◄────────────────────────────┘ ││
│   │   └── error.rs ◄──────────────────────────────┘│
│   └── tree.rs ◄─────────────────────────────────┐  │
│       ├── types.rs ◄────────────────────────────┘  │
│       ├── error.rs ◄───────────────────────────────┘
│       ├── node.rs ◄────────────────────────────────┐
│       └── arena.rs ◄───────────────────────────────┘
│
├── operations/
│   ├── mod.rs
│   ├── construction.rs ◄───────────────────────────┐
│   ├── insert.rs ◄─────────────────────────────────┤
│   ├── delete.rs ◄─────────────────────────────────┤
│   └── query.rs ◄──────────────────────────────────┤
│       └── core/* ◄────────────────────────────────┘
│
├── traversal/
│   ├── mod.rs
│   ├── iteration.rs ◄──────────────────────────────┐
│   │   └── core::node ◄────────────────────────────┘
│   └── range.rs ◄──────────────────────────────────┐
│       └── traversal::iteration ◄──────────────────┘
│
├── utils/
│   ├── mod.rs
│   ├── macros.rs (no deps)
│   └── helpers.rs ◄────────────────────────────────┐
│       └── types.rs ◄──────────────────────────────┘
│
└── validation/
    ├── mod.rs
    └── invariants.rs ◄─────────────────────────────┐
        └── all modules ◄───────────────────────────┘
```

### 3.2 의존성 규칙

#### 계층별 의존성 규칙

```
Layer 0: types.rs, error.rs (Foundation)
    ↑
Layer 1: utils/macros.rs (Utilities)
    ↑
Layer 2: core/arena.rs, core/node.rs (Core Primitives)
    ↑
Layer 3: core/tree.rs (Structure)
    ↑
Layer 4: operations/* (Operations)
    ↑
Layer 5: traversal/* (Iteration)
    ↑
Layer 6: validation/* (Validation)
    ↑
Layer 7: lib.rs (Public API)
```

**규칙:**
1. **단방향 의존성**: 상위 레이어는 하위 레이어만 의존할 수 있음
2. **동일 레이어 의존 금지**: 같은 레이어 내 모듈 간 직접 의존 최소화
3. **Foundation 불변성**: `types.rs`, `error.rs`는 어떤 모듈에도 의존하지 않음
4. **Validation 최상위**: `validation`은 모든 모듈에 의존 가능 (테스트/디버깅 목적)

### 3.3 순환 의존성 방지 규칙

```rust
// ❌ 금지: 순환 의존성
// node.rs -> tree.rs -> node.rs

// ✅ 허용: 단방향 의존성
// node.rs (기본 연산)
// tree.rs -> node.rs (고수준 연산)
```

---

## 4. 모듈별 변경 빈도 분류

### 4.1 변경 빈도 매트릭스

| 분류 | 모듈 | 변경 빈도 | 변경 원인 | 영향 범위 |
|------|------|-----------|-----------|-----------|
| **안정적** | `types.rs` | 매우 낮음 | 타입 정의 변경 | 전체 (파급력 큼) |
| **안정적** | `utils/macros.rs` | 매우 낮음 | 매크로 추가 | 제한적 |
| **안정적** | `error.rs` | 낮음 | 새로운 에러 타입 | 중간 |
| **안정적** | `core/arena.rs` | 낮음 | 메모리 관리 최적화 | 중간 |
| **안정적** | `operations/construction.rs` | 매우 낮음 | 생성자 변경 | 낮음 |
| **안정적** | `lib.rs` | 낮음 | 공개 API 변경 | 전체 |
| **진화적** | `core/node.rs` | 중간 | 노드 로직 개선 | 중간 |
| **진화적** | `operations/insert.rs` | 중간 | 삽입 알고리즘 개선 | 중간 |
| **진화적** | `operations/delete.rs` | 중간 | 삭제/재조정 개선 | 중간 |
| **진화적** | `traversal/iteration.rs` | 중간 | 이터레이터 최적화 | 중간 |
| **진화적** | `validation/invariants.rs` | 중간 | 새로운 검증 추가 | 낮음 (테스트용) |
| **진화적** | `core/tree.rs` | 낮음 | 트리 구조 변경 | 중간 |
| **진화적** | `operations/query.rs` | 낮음 | 조회 최적화 | 낮음 |
| **진화적** | `traversal/range.rs` | 낮음 | 범위 쿼리 개선 | 낮음 |
| **진화적** | `utils/helpers.rs` | 낮음 | 헬퍼 함수 추가 | 낮음 |

### 4.2 변경 영향도 분석

```
┌─────────────────────────────────────────────────────────────────┐
│                     변경 영향도 다이어그램                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   [안정적]                                                       │
│   ┌──────────┐  ┌──────────┐  ┌──────────┐                      │
│   │ types.rs │  │ error.rs │  │ macros.rs│                      │
│   └────┬─────┘  └────┬─────┘  └────┬─────┘                      │
│        │             │             │                            │
│        ▼             ▼             ▼                            │
│   ┌──────────────────────────────────────┐                      │
│   │         [core 모듈]                   │                      │
│   │  ┌──────────┐  ┌──────────┐         │                      │
│   │  │ arena.rs │  │ node.rs  │         │                      │
│   │  └──────────┘  └────┬─────┘         │                      │
│   │                     │               │                      │
│   │              ┌──────┴──────┐        │                      │
│   │              │  tree.rs    │        │                      │
│   │              └─────────────┘        │                      │
│   └──────────────────┬──────────────────┘                      │
│                      │                                          │
│        ┌─────────────┼─────────────┐                           │
│        ▼             ▼             ▼                           │
│   ┌──────────┐  ┌──────────┐  ┌──────────┐                    │
│   │operations│  │traversal │  │validation│                    │
│   └──────────┘  └──────────┘  └──────────┘                    │
│        │             │             │                           │
│        └─────────────┴─────────────┘                           │
│                      │                                          │
│                      ▼                                          │
│               ┌──────────┐                                     │
│               │ lib.rs   │  [Public API]                       │
│               └──────────┘                                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. 공개 API 설계 초안

### 5.1 공개 타입

```rust
// src/lib.rs

//! B+ Tree implementation in Rust with dict-like API

// 모듈 선언
mod core;
mod error;
mod operations;
mod traversal;
mod types;
mod utils;
mod validation;

// 공개 re-export
pub use core::arena::{CompactArena, CompactArenaStats};
pub use error::{BPlusTreeError, BTreeResult, BTreeResultExt, ModifyResult};
pub use traversal::iteration::{FastItemIterator, ItemIterator, KeyIterator, ValueIterator};
pub use traversal::range::RangeIterator;
pub use types::{BPlusTreeMap, BranchNode, LeafNode, NodeId, NodeRef, NULL_NODE, ROOT_NODE};

// 주요 구현은 lib.rs에 위치
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    // 생성자
    pub fn new(capacity: usize) -> Result<Self, BPlusTreeError>;
    pub fn with_capacity(capacity: usize) -> Result<Self, BPlusTreeError>;
    
    // 기본 연산
    pub fn insert(&mut self, key: K, value: V) -> Option<V>;
    pub fn remove(&mut self, key: &K) -> Option<V>;
    pub fn get(&self, key: &K) -> Option<&V>;
    pub fn get_mut(&mut self, key: &K) -> Option<&mut V>;
    pub fn contains_key(&self, key: &K) -> bool;
    
    // 고급 API
    pub fn try_insert(&mut self, key: K, value: V) -> ModifyResult<Option<V>>;
    pub fn try_remove(&mut self, key: &K) -> ModifyResult<V>;
    pub fn batch_insert(&mut self, items: Vec<(K, V)>) -> ModifyResult<Vec<Option<V>>>;
    
    // 순회
    pub fn iter(&self) -> ItemIterator<K, V>;
    pub fn keys(&self) -> KeyIterator<K, V>;
    pub fn values(&self) -> ValueIterator<K, V>;
    pub fn range<R>(&self, range: R) -> RangeIterator<K, V>
    where
        R: std::ops::RangeBounds<K>;
    
    // 상태 조회
    pub fn len(&self) -> usize;
    pub fn is_empty(&self) -> bool;
    pub fn capacity(&self) -> usize;
    pub fn node_count(&self) -> (usize, usize); // (leaves, branches)
    
    // 유틸리티
    pub fn clear(&mut self);
    pub fn check_invariants(&self) -> Result<(), String>;
}

// 표준 트레이트 구현
impl<K: Ord + Clone, V: Clone> Default for BPlusTreeMap<K, V>;
impl<K: Ord + Clone, V: Clone> Clone for BPlusTreeMap<K, V>;
impl<K: Ord + Clone, V: Clone> Extend<(K, V)> for BPlusTreeMap<K, V>;
impl<K: Ord + Clone, V: Clone> FromIterator<(K, V)> for BPlusTreeMap<K, V>;
impl<K: Ord + Clone, V: Clone> IntoIterator for BPlusTreeMap<K, V>;
impl<K: Ord + Clone, V: Clone> IntoIterator for &BPlusTreeMap<K, V>;
```

### 5.2 모듈별 공개 인터페이스

#### core 모듈

```rust
// src/core/mod.rs
pub mod arena;
pub mod node;
pub mod tree;

// 낶부용 re-export
pub(crate) use arena::CompactArena;
pub(crate) use node::{BranchNode, LeafNode};
pub(crate) use tree::TreeStructure;
```

#### operations 모듈

```rust
// src/operations/mod.rs
pub mod construction;
pub mod delete;
pub mod insert;
pub mod query;

// 낶부용 re-export
pub(crate) use construction::TreeConstructor;
pub(crate) use delete::DeleteOperations;
pub(crate) use insert::InsertOperations;
pub(crate) use query::QueryOperations;
```

#### traversal 모듈

```rust
// src/traversal/mod.rs
pub mod iteration;
pub mod range;

// 공개 re-export
pub use iteration::{ItemIterator, KeyIterator, ValueIterator, FastItemIterator};
pub use range::RangeIterator;
```

#### validation 모듈

```rust
// src/validation/mod.rs
pub mod invariants;

// 공개 re-export
pub use invariants::InvariantChecker;
```

### 5.3 API 안정성 분류

| API | 안정성 | 변경 가능성 | 비고 |
|-----|--------|-------------|------|
| `BPlusTreeMap::new` | Stable | 없음 | 핵심 생성자 |
| `BPlusTreeMap::insert` | Stable | 없음 | 핵심 연산 |
| `BPlusTreeMap::remove` | Stable | 없음 | 핵심 연산 |
| `BPlusTreeMap::get` | Stable | 없음 | 핵심 연산 |
| `BPlusTreeMap::iter` | Stable | 없음 | 핵심 순회 |
| `BPlusTreeMap::range` | Stable | 없음 | 핵심 쿼리 |
| `BPlusTreeMap::try_insert` | Stable | 낮음 | 고급 API |
| `BPlusTreeMap::batch_insert` | Experimental | 높음 | API 개선 가능 |
| `FastItemIterator` | Experimental | 중간 | 구현 세부사항 |
| `CompactArenaStats` | Unstable | 높음 | 낶부용 |

---

## 6. 구현 지침

### 6.1 파일 크기 관리

```rust
// 크기 제한 초과 시 분할 전략

// ❌ 피할 것: 한 파일에 모든 구현
// src/operations.rs (1200줄)

// ✅ 권장: 기능별 분리
// src/operations/
// ├── mod.rs        (공용 코드, ~100줄)
// ├── insert.rs     (삽입, ~400줄)
// ├── delete.rs     (삭제, ~600줄)
// └── query.rs      (조회, ~200줄)
```

### 6.2 모듈 선언 패턴

```rust
// src/core/mod.rs

// 1. 서브모듈 선언
mod arena_impl;
mod node_impl;
mod tree_impl;

// 2. 공개 re-export
pub use arena_impl::CompactArena;
pub use node_impl::{BranchNode, LeafNode};
pub use tree_impl::TreeStructure;

// 3. 낶부용 re-export
pub(crate) use arena_impl::ArenaStats;

// 4. 모듈 공용 트레이트 (선택적)
pub(crate) trait NodeOperations<K, V> {
    fn is_full(&self) -> bool;
    fn is_underfull(&self) -> bool;
}
```

### 6.3 의존성 주입 패턴

```rust
// ❌ 피할 것: 직접 의존
// insert_operations.rs
use crate::core::node::LeafNode;

// ✅ 권장: 모듈 수준 추상화
// insert_operations.rs
use crate::core::{LeafNode, NodeOperations};
```

---

## 7. 품질 게이트

### 7.1 구조적 품질 기준

| 기준 | 측정 방법 | 최소 기준 |
|------|-----------|-----------|
| 파일 크기 | `wc -l` | 최대 600줄 |
| 함수 크기 | 라인 수 | 최대 50줄 |
| 모듈 의존성 | `cargo modules` | 순환 의존성 없음 |
| 공개 API | 문서화 | 100% 문서화 |
| 테스트 커버리지 | `cargo tarpaulin` | 80% 이상 |

### 7.2 코드 리뷰 체크리스트

- [ ] 파일 크기가 제한을 초과하지 않는가?
- [ ] 새로운 모듈이 올바른 레이어에 있는가?
- [ ] 의존성이 단방향인가?
- [ ] 순환 의존성이 없는가?
- [ ] 공개 API가 문서화되었는가?
- [ ] 변경 빈도가 적절히 분류되었는가?

---

## 8. 참고 문서

- [Architecture Decisions](architecture-decisions.md)
- [Code Structure Analysis](code-structure-analysis.md)
- [Development Feedback Loops](development-feedback-loops.md)
- [Lessons Learned](lessons-learned.md)

---

**문서 버전:** 1.0  
**작성자:** code-designer 에이전트  
**검토자:** (team-lead 검토 대기)
