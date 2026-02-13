# 코드 구조 심층 분석

## 개요

BPlusTree3 러스트 구현의 코드 구조를 파일별로 분석하여, 각 모듈의 책임, 인터페이스, 그리고 상호작용을 상세히 설명합니다.

---

## 1. 전체 모듈 구조

```
rust/src/
├── lib.rs                    # 공개 API 및 모듈 선언
├── types.rs                  # 핵심 타입 정의
├── error.rs                  # 에러 처리
├── node.rs                   # 노드 구현 (Leaf + Branch)
├── tree_structure.rs         # 트리 구조 관리
├── construction.rs           # 생성자 구현
├── insert_operations.rs      # 삽입 연산
├── delete_operations.rs      # 삭제 연산
├── get_operations.rs         # 조회 연산
├── iteration.rs              # 이터레이터 구현
├── range_queries.rs          # 범위 쿼리
├── validation.rs             # 검증 및 디버깅
├── compact_arena.rs          # 아레나 메모리 관리
└── macros.rs                 # 유틸리티 매크로
```

---

## 2. 파일별 상세 분석

### 2.1 lib.rs - 공개 API

**책임**: 모듈 선언, 공개 API 노출, 문서화

**크기**: ~200줄

**핵심 구조**:
```rust
//! B+ Tree implementation in Rust with dict-like API.

// 모듈 선언
mod compact_arena;
mod construction;
mod delete_operations;
mod error;
mod get_operations;
mod insert_operations;
mod iteration;
mod macros;
mod node;
mod range_queries;
mod tree_structure;
mod types;
mod validation;

// 공개 re-export
pub use compact_arena::{CompactArena, CompactArenaStats};
pub use error::{BPlusTreeError, BTreeResult, BTreeResultExt, ModifyResult};
pub use iteration::{FastItemIterator, ItemIterator, KeyIterator, RangeIterator, ValueIterator};
pub use types::{BPlusTreeMap, BranchNode, LeafNode, NodeId, NodeRef, NULL_NODE, ROOT_NODE};

// BPlusTreeMap 구현 (impl 블록)
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    // 고급 API: try_insert, try_remove, batch_insert 등
    pub fn try_insert(&mut self, key: K, value: V) -> ModifyResult<Option<V>> { ... }
    pub fn try_remove(&mut self, key: &K) -> ModifyResult<V> { ... }
    pub fn batch_insert(&mut self, items: Vec<(K, V)>) -> ModifyResult<Vec<Option<V>>> { ... }
}
```

**설계 원칙**:
- `lib.rs`는 진입점 역할만 하며, 실제 구현은 각 모듈에 위임
- 공개 API는 명확하고 문서화됨
- 낮은 결합도: 모듈 간 직접 의존 최소화

---

### 2.2 types.rs - 핵심 타입

**책임**: 모든 핵심 타입 정의, 상수

**크기**: ~100줄

**핵심 타입**:
```rust
pub type NodeId = usize;
pub const NULL_NODE: NodeId = usize::MAX;
pub const ROOT_NODE: NodeId = 0;
pub const MIN_CAPACITY: usize = 2;

/// 트리의 모든 노드를 참조하는 열거형
pub enum NodeRef<K, V> {
    Leaf(NodeId, PhantomData<(K, V)>),
    Branch(NodeId, PhantomData<(K, V)>),
}

/// B+ Tree 메인 구조
pub struct BPlusTreeMap<K, V> {
    pub(crate) root: NodeRef<K, V>,
    pub(crate) leaf_arena: CompactArena<LeafNode<K, V>>,
    pub(crate) branch_arena: CompactArena<BranchNode<K, V>>,
    pub(crate) capacity: usize,
}

/// Leaf 노드 구조
pub struct LeafNode<K, V> {
    pub capacity: usize,
    pub keys: Vec<K>,
    pub values: Vec<V>,
    pub next: NodeId,  // 연결 리스트 포인터
}

/// Branch (낶부) 노드 구조
pub struct BranchNode<K, V> {
    pub capacity: usize,
    pub keys: Vec<K>,
    pub children: Vec<NodeRef<K, V>>,
}

/// 삽입 결과
pub enum InsertResult<K, V> {
    Updated(Option<V>),
    Split { old_value: Option<V>, new_node_data: SplitNodeData<K, V>, separator_key: K },
}

pub enum SplitNodeData<K, V> {
    Leaf(LeafNode<K, V>),
    Branch(BranchNode<K, V>),
}
```

**설계 원칙**:
- 타입 정의는 안정적이며 자주 변경되지 않음
- PhantomData를 사용하여 타입 안전성 확보
- crate 전반에서 사용되는 기본 타입만 정의

---

### 2.3 error.rs - 에러 처리

**책임**: 모든 에러 타입과 Result 타입 별칭

**크기**: ~200줄

**핵심 구조**:
```rust
#[derive(Debug, Clone, PartialEq)]
pub enum BPlusTreeError {
    InvalidCapacity { requested: usize, min: usize },
    KeyNotFound,
    DataIntegrityError(String),
}

// Result 타입 별칭
pub type BTreeResult<T> = Result<T, BPlusTreeError>;
pub type KeyResult<T> = Result<T, BPlusTreeError>;
pub type ModifyResult<T> = Result<T, BPlusTreeError>;
pub type InitResult<T> = Result<T, BPlusTreeError>;

// 확장 트레이트
pub trait BTreeResultExt<T> {
    fn or_key_not_found(self) -> Result<T, BPlusTreeError>;
    fn validate_integrity(self) -> Result<T, BPlusTreeError>;
}

impl<T> fmt::Display for BPlusTreeError { ... }
impl std::error::Error for BPlusTreeError { ... }
```

**설계 원칙**:
- thiserror 크레이트를 사용하지 않고 수동 구현 (의존성 최소화)
- 모든 에러는 Clone 가능 (롤백 등에서 필요)
- 세분화된 에러 타입으로 다른 대응 가능

---

### 2.4 node.rs - 노드 구현

**책임**: LeafNode와 BranchNode의 모든 메서드

**크기**: ~600줄

**LeafNode 구현**:
```rust
impl<K: Ord + Clone, V: Clone> LeafNode<K, V> {
    // GET OPERATIONS
    pub fn get(&self, key: &K) -> Option<&V>;
    pub fn get_mut(&mut self, key: &K) -> Option<&mut V>;
    pub fn len(&self) -> usize;

    // UNSAFE ACCESSORS (성능 최적화)
    pub unsafe fn get_key_unchecked(&self, index: usize) -> &K;
    pub unsafe fn get_value_unchecked(&self, index: usize) -> &V;
    pub unsafe fn get_key_value_unchecked(&self, index: usize) -> (&K, &V);

    // INSERT OPERATIONS
    pub fn insert(&mut self, key: K, value: V) -> InsertResult<K, V>;
    pub fn insert_at_index(&mut self, index: usize, key: K, value: V);
    pub fn split(&mut self) -> LeafNode<K, V>;

    // DELETE OPERATIONS
    pub fn remove(&mut self, key: &K) -> (Option<V>, bool); // (value, is_underfull)

    // STATUS CHECKS
    pub fn is_full(&self) -> bool;
    pub fn is_underfull(&self) -> bool;
    pub fn can_donate(&self) -> bool;
    pub fn min_keys(&self) -> usize;

    // BORROWING AND MERGING
    pub fn borrow_last(&mut self) -> Option<(K, V)>;
    pub fn borrow_first(&mut self) -> Option<(K, V)>;
    pub fn merge_from(&mut self, other: &mut LeafNode<K, V>) -> NodeId;
}
```

**BranchNode 구현**:
```rust
impl<K: Ord + Clone, V: Clone> BranchNode<K, V> {
    pub fn insert_child_and_split_if_needed(
        &mut self,
        child_index: usize,
        separator_key: K,
        new_child: NodeRef<K, V>,
    ) -> Option<(BranchNode<K, V>, K)>;

    pub fn split_data(&mut self) -> (BranchNode<K, V>, K);
    pub fn find_child_index(&self, key: &K) -> usize;
    pub fn is_full(&self) -> bool;
    pub fn is_underfull(&self) -> bool;
    pub fn min_keys(&self) -> usize;

    // BORROWING AND MERGING
    pub fn borrow_last(&mut self) -> Option<(K, NodeRef<K, V>)>;
    pub fn borrow_first(&mut self) -> Option<(K, NodeRef<K, V>)>;
    pub fn merge_from(&mut self, separator: K, other: &mut BranchNode<K, V>);
}
```

**설계 원칙**:
- 각 노드 타입은 자신의 데이터 관리에만 책임
- 분할/병합/빌리기 로직은 노드 내부에 캡슐화
- Unsafe 메서드는 명확한 Safety invariant 문서화

---

### 2.5 tree_structure.rs - 트리 구조 관리

**책임**: 트리 전체의 구조적 연산

**크기**: ~200줄

**핵심 메서드**:
```rust
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    // SIZE QUERIES
    pub fn len(&self) -> usize;
    pub fn is_empty(&self) -> bool;
    pub fn leaf_count(&self) -> usize;
    pub fn count_nodes_in_tree(&self) -> (usize, usize); // (leaves, branches)

    // TREE STRUCTURE
    pub fn clear(&mut self);
    pub fn is_leaf_root(&self) -> bool;

    // NAVIGATION HELPERS
    pub fn get_first_leaf_id(&self) -> Option<NodeId>;
    pub fn find_leaf_for_key(&self, key: &K) -> Option<(NodeId, usize)>;
    pub fn find_leaf_for_key_with_match(&self, key: &K) -> Option<(NodeId, usize, bool)>;
    pub fn find_child(&self, branch_id: NodeId, key: &K) -> Option<(usize, NodeRef<K, V>)>;
}
```

**설계 원칙**:
- 트리 전체의 구조적 정보 제공
- 내부 네비게이션 로직 캡슐화
- 재귀적 계산으로 항상 정확한 상태

---

### 2.6 construction.rs - 생성자

**책임**: 트리 및 노드 생성

**크기**: ~150줄

**핵심 구현**:
```rust
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    pub fn new(capacity: usize) -> Result<Self, BPlusTreeError> {
        if capacity < MIN_CAPACITY {
            return Err(BPlusTreeError::InvalidCapacity { requested: capacity, min: MIN_CAPACITY });
        }

        let mut leaf_arena = CompactArena::new(capacity);
        let root_leaf = LeafNode::new(capacity);
        let root_id = leaf_arena.allocate(root_leaf);

        Ok(BPlusTreeMap {
            root: NodeRef::Leaf(root_id, PhantomData),
            leaf_arena,
            branch_arena: CompactArena::new(capacity),
            capacity,
        })
    }
}

impl<K: Clone, V: Clone> LeafNode<K, V> {
    pub fn new(capacity: usize) -> Self {
        Self {
            capacity,
            keys: Vec::with_capacity(capacity),
            values: Vec::with_capacity(capacity),
            next: NULL_NODE,
        }
    }
}

impl<K: Clone, V: Clone> BranchNode<K, V> {
    pub fn new(capacity: usize) -> Self {
        Self {
            capacity,
            keys: Vec::with_capacity(capacity),
            children: Vec::with_capacity(capacity + 1),
        }
    }
}
```

**설계 원칙**:
- 모든 생성 로직을 한 곳에 집중
- 유효성 검사는 생성 시점에 수행
- Arena 할당과 초기화 통합

---

### 2.7 insert_operations.rs - 삽입 연산

**책임**: 키-값 삽입 및 노드 분할

**크기**: ~400줄

**핵심 알고리즘**:
```rust
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    pub fn insert(&mut self, key: K, value: V) -> Option<V> {
        let (result, root_split) = self.insert_recursive(self.root.clone(), key, value);

        if let Some((new_node, separator)) = root_split {
            self.handle_root_split(new_node, separator);
        }

        result
    }

    fn insert_recursive(
        &mut self,
        node: NodeRef<K, V>,
        key: K,
        value: V,
    ) -> (Option<V>, Option<(SplitNodeData<K, V>, K)>) {
        match node {
            NodeRef::Leaf(leaf_id, _) => {
                let leaf = self.leaf_arena.get_mut(leaf_id).unwrap();
                match leaf.insert(key, value) {
                    InsertResult::Updated(old) => (old, None),
                    InsertResult::Split { old_value, new_node_data, separator_key } => {
                        (old_value, Some((new_node_data, separator_key)))
                    }
                }
            }
            NodeRef::Branch(branch_id, _) => {
                // 적절한 자식 찾기
                let child_index = self.find_child_index(branch_id, &key);
                let child = self.get_branch_child(branch_id, child_index);

                // 재귀적 삽입
                let (result, child_split) = self.insert_recursive(child, key, value);

                if let Some((new_node, separator)) = child_split {
                    // 자식이 분할됨 - 이 branch에 삽입
                    self.handle_child_split(branch_id, child_index, new_node, separator);
                }

                (result, None)
            }
        }
    }
}
```

**설계 원칙**:
- 재귀적 구현으로 깔끔한 로직
- 분할은 하위에서 상위로 전파
- Root 분할은 별도 처리

---

### 2.8 delete_operations.rs - 삭제 연산

**책임**: 키 삭제 및 트리 재조정

**크기**: ~600줄 (가장 복잡한 모듈)

**핵심 알고리즘**:
```rust
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    pub fn remove(&mut self, key: &K) -> Option<V> {
        let (value, needs_rebalance) = self.remove_recursive(&self.root.clone(), key);

        if needs_rebalance {
            self.rebalance_after_remove();
        }

        // Root가 underfull이면 처리
        self.collapse_root_if_needed();

        value
    }

    fn remove_recursive(
        &mut self,
        node_ref: &NodeRef<K, V>,
        key: &K,
    ) -> (Option<V>, bool) {
        match node_ref {
            NodeRef::Leaf(leaf_id, _) => {
                let leaf = self.leaf_arena.get_mut(*leaf_id).unwrap();
                let (value, is_underfull) = leaf.remove(key);
                (value, is_underfull)
            }
            NodeRef::Branch(branch_id, _) => {
                let child_index = self.find_child_index(*branch_id, key);
                let child = self.get_branch_child(*branch_id, child_index);

                let (value, child_needs_rebalance) = self.remove_recursive(&child, key);

                if child_needs_rebalance {
                    self.rebalance_child(*branch_id, child_index);
                }

                (value, self.is_branch_underfull(*branch_id))
            }
        }
    }

    // 재조정 전략
    fn rebalance_child(&mut self, parent_id: NodeId, child_index: usize) {
        // 1. Try borrowing from left sibling
        if self.try_borrow_from_left(parent_id, child_index) {
            return;
        }

        // 2. Try borrowing from right sibling
        if self.try_borrow_from_right(parent_id, child_index) {
            return;
        }

        // 3. Must merge
        if child_index > 0 {
            self.merge_with_left(parent_id, child_index);
        } else {
            self.merge_with_right(parent_id, child_index);
        }
    }
}
```

**재조정 전략** (우선순위 순):
1. 왼쪽 sibling에서 빌리기 (rotate)
2. 오른쪽 sibling에서 빌리기 (rotate)
3. 왼쪽과 병합
4. 오른쪽과 병합

**설계 원칙**:
- 가장 간단한 해결책부터 시도 (빌리기가 병합보다 쉬움)
- 재조정은 상위로 전파될 수 있음
- Root collapse는 별도 처리

---

### 2.9 get_operations.rs - 조회 연산

**책임**: 키로 값 조회

**크기**: ~200줄

**핵심 구현**:
```rust
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    pub fn get(&self, key: &K) -> Option<&V> {
        match self.find_leaf_for_key(key) {
            Some((leaf_id, index, true)) => {
                let leaf = self.get_leaf(leaf_id)?;
                leaf.get_value(index)
            }
            _ => None,
        }
    }

    pub fn get_mut(&mut self, key: &K) -> Option<&mut V> {
        match self.find_leaf_for_key(key) {
            Some((leaf_id, index, true)) => {
                let leaf = self.get_leaf_mut(leaf_id)?;
                leaf.get_value_mut(index)
            }
            _ => None,
        }
    }

    pub fn contains_key(&self, key: &K) -> bool {
        self.get(key).is_some()
    }

    // Batch lookup optimization
    pub fn get_many(&self, keys: &[K]) -> Vec<Option<&V>> {
        keys.iter().map(|k| self.get(k)).collect()
    }
}
```

**설계 원칙**:
- find_leaf_for_key 재사용으로 코드 중복 방지
- Batch lookup으로 반복 최적화
- Simple and direct

---

### 2.10 iteration.rs - 이터레이터

**책임**: 다양한 종류의 이터레이터 구현

**크기**: ~400줄

**핵심 이터레이터들**:
```rust
// 1. ItemIterator - (key, value) 쌍
pub struct ItemIterator<'a, K, V> {
    tree: &'a BPlusTreeMap<K, V>,
    current_leaf_id: Option<NodeId>,
    current_leaf_ref: Option<&'a LeafNode<K, V>>, // 캐싱!
    current_index: usize,
}

// 2. FastItemIterator - Unsafe 최적화 버전
pub struct FastItemIterator<'a, K, V> { ... }

// 3. KeyIterator - 키만
pub struct KeyIterator<'a, K, V>(ItemIterator<'a, K, V>);

// 4. ValueIterator - 값만
pub struct ValueIterator<'a, K, V>(ItemIterator<'a, K, V>);
```

**Leaf Caching 최적화**:
```rust
impl<'a, K: Ord, V> Iterator for ItemIterator<'a, K, V> {
    type Item = (&'a K, &'a V);

    fn next(&mut self) -> Option<Self::Item> {
        // Leaf reference 캐싱으로 반복 arena lookup 방지
        if self.current_leaf_ref.is_none() {
            self.current_leaf_ref = self.tree.get_leaf(self.current_leaf_id?);
        }

        if let Some(leaf) = self.current_leaf_ref {
            if self.current_index < leaf.keys_len() {
                let (k, v) = unsafe {
                    leaf.get_key_value_unchecked(self.current_index)
                };
                self.current_index += 1;
                return Some((k, v));
            }

            // 다음 leaf로 이동
            let next_id = leaf.next;
            if next_id != NULL_NODE {
                self.current_leaf_id = Some(next_id);
                self.current_leaf_ref = self.tree.get_leaf(next_id);
                self.current_index = 0;
                return self.next();
            }
        }
        None
    }
}
```

**설계 원칙**:
- Leaf caching으로 반복 최적화
- Linked list 순회로 O(1) next()
- Adapter pattern으로 Key/Value 이터레이터 구현

---

### 2.11 range_queries.rs - 범위 쿼리

**책임**: 범위 기반 순회 구현

**크기**: ~300줄

**핵심 구현**:
```rust
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    pub fn range<R>(&self, range: R) -> RangeIterator<K, V>
    where
        R: RangeBounds<K>,
    {
        let (start_id, start_index) = self.find_range_start(&range);

        RangeIterator {
            tree: self,
            current_leaf_id: start_id,
            current_index: start_index,
            end_bound: range.end_bound().cloned(),
        }
    }
}

pub struct RangeIterator<'a, K, V> {
    tree: &'a BPlusTreeMap<K, V>,
    current_leaf_id: Option<NodeId>,
    current_index: usize,
    end_bound: Bound<&'a K>,
}

impl<'a, K: Ord, V> Iterator for RangeIterator<'a, K, V> {
    type Item = (&'a K, &'a V);

    fn next(&mut self) -> Option<Self::Item> {
        // end_bound 체크
        // 현재 leaf에서 아이템 반환
        // 다음 leaf로 이동 (linked list)
    }
}
```

**설계 원칙**:
- Hybrid navigation: Tree 탐색 + Linked list 순회
- RangeBounds 트레이트 구현으로 다양한 문법 지원
- O(log n + k) 범위 쿼리

---

### 2.12 validation.rs - 검증

**책임**: 트리 불변식 검증, 디버깅 유틸리티

**크기**: ~400줄

**핵심 검증**:
```rust
impl<K: Ord + Clone, V: Clone> BPlusTreeMap<K, V> {
    /// 모든 불변식 검증
    pub fn check_invariants(&self) -> Result<(), String> {
        self.check_invariants_recursive(&self.root, None, None)
    }

    fn check_invariants_recursive(
        &self,
        node: &NodeRef<K, V>,
        lower: Option<&K>,
        upper: Option<&K>,
    ) -> Result<(), String> {
        match node {
            NodeRef::Leaf(leaf_id, _) => {
                let leaf = self.get_leaf(*leaf_id)
                    .ok_or("Invalid leaf reference")?;

                // 1. 키 정렬 검증
                if !leaf.keys.windows(2).all(|w| w[0] < w[1]) {
                    return Err("Leaf keys not sorted".to_string());
                }

                // 2. 범위 검증
                for key in &leaf.keys {
                    if let Some(l) = lower { if key <= l { return Err("Key below lower bound"); } }
                    if let Some(u) = upper { if key >= u { return Err("Key above upper bound"); } }
                }

                // 3. 키-값 개수 일치
                if leaf.keys.len() != leaf.values.len() {
                    return Err("Keys and values length mismatch");
                }

                Ok(())
            }
            NodeRef::Branch(branch_id, _) => {
                // Branch 검증...
            }
        }
    }

    /// 상세 검증 (디버깅용)
    pub fn check_invariants_detailed(&self) -> Result<(), String> { ... }

    /// 트리 구조 출력
    pub fn debug_print(&self) { ... }
}
```

**설계 원칙**:
- 모든 불변식을 포괄적으로 검증
- 상세한 에러 메시지로 문제 위치 식별
- 테스트와 디버깅에 필수적

---

### 2.13 compact_arena.rs - 아레나 메모리

**책임**: 효율적인 노드 메모리 관리

**크기**: ~300줄

**핵심 구조**:
```rust
pub struct CompactArena<T> {
    items: Vec<Option<T>>,      // 실제 데이터
    free_list: Vec<usize>,      // 재사용 가능한 인덱스
    len: usize,                 // 현재 사용 중
}

pub struct CompactArenaStats {
    pub capacity: usize,        // 총 슬롯 수
    pub allocated: usize,       // 사용 중인 슬롯
    pub free_list_size: usize,  // 재사용 가능
}

impl<T> CompactArena<T> {
    pub fn new(capacity: usize) -> Self { ... }

    pub fn allocate(&mut self, item: T) -> usize {
        if let Some(id) = self.free_list.pop() {
            // 재사용
            self.items[id] = Some(item);
            self.len += 1;
            id
        } else {
            // 새로 할당
            let id = self.items.len();
            self.items.push(Some(item));
            self.len += 1;
            id
        }
    }

    pub fn deallocate(&mut self, id: usize) -> Option<T> {
        if let Some(item) = self.items[id].take() {
            self.free_list.push(id);
            self.len -= 1;
            Some(item)
        } else {
            None
        }
    }

    pub fn get(&self, id: usize) -> Option<&T> { ... }
    pub fn get_mut(&mut self, id: usize) -> Option<&mut T> { ... }
    pub fn stats(&self) -> CompactArenaStats { ... }
}
```

**설계 원칙**:
- Vec<Option<T>>로 메모리 연속성 확보
- Free list로 재사용 가능한 슬롯 관리
- O(1) allocate/deallocate/get

---

## 3. 모듈 간 상호작용

### 3.1 의존성 그래프

```
lib.rs (public API)
├── types.rs ◄────────────────────────────┐
├── error.rs ◄───────────────────────────┐│
├── compact_arena.rs ◄──────────────────┐││
│   └── types.rs                        │││
├── node.rs ◄──────────────────────────┐│││
│   ├── types.rs ◄─────────────────────┘│││
│   └── error.rs ◄──────────────────────┘││
├── tree_structure.rs ◄───────────────┐ │││
│   └── node.rs ◄─────────────────────┘ ││
├── construction.rs ◄────────────────┐ ││
│   └── tree_structure.rs ◄──────────┘ │
├── insert_operations.rs ◄──────────┐
│   └── node.rs ◄───────────────────┘
├── delete_operations.rs ◄─────────┐
│   └── node.rs ◄──────────────────┘
├── get_operations.rs ◄───────────┐
│   └── tree_structure.rs ◄───────┘
├── iteration.rs ◄───────────────┐
│   └── node.rs ◄────────────────┘
├── range_queries.rs ◄──────────┐
│   └── iteration.rs ◄──────────┘
└── validation.rs ◄────────────┐
    └── all modules ◄──────────┘
```

### 3.2 데이터 흐름

```
사용자 요청
    ↓
lib.rs (public API)
    ↓
[get/insert/remove]_operations.rs
    ↓
tree_structure.rs (navigation)
    ↓
node.rs (node-level operations)
    ↓
compact_arena.rs (memory access)
    ↓
LeafNode/BranchNode (data)
```

---

## 4. 설계 패턴

### 4.1 Arena Pattern
- 메모리 할당의 효율성과 예측 가능성
- 노드 ID를 통한 간접 참조

### 4.2 Module Separation by Change Frequency
- 함께 변경되는 코드끼리 그룹화
- 변경 영향 범위 최소화

### 4.3 PhantomData for Type Safety
- 컴파일 타임 타입 검사
- 제로코스트 추상화

### 4.4 Iterator Pattern
- 표준 Rust iterator 인터페이스 구현
- Leaf caching으로 성능 최적화

### 4.5 Result-Based Error Handling
- 타입 안전한 에러 처리
- 복구 가능성 명시

---

## 5. 성능 특성

| 모듈 | 시간 복잡도 | 공간 복잡도 | 비고 |
|------|-------------|-------------|------|
| insert_operations | O(log n) | O(1) | 분할 시 O(log n) 추가 |
| delete_operations | O(log n) | O(1) | 재조정 시 O(log n) 추가 |
| get_operations | O(log n) | O(1) | |
| iteration.rs | O(1) amortized | O(1) | Leaf caching |
| range_queries | O(log n + k) | O(1) | k = 결과 개수 |
| validation | O(n) | O(log n) | 재귀 스택 |
