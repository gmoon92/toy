# Tree

트리 구조란 "그래프 이론"을 접목시킨 자료 구조로써, 한 노드에서 시작해서 다른 정점들을 순회하여 자기 자신에게 돌아오는 순환이 없는 연결 그래프이다.

> 그래프 이론이란 짝을 이루는 데이터의 관계를 모델링 위해, 간선과 점으로 구성되며, 데이터 관계에 대해 방향성이 존재하거나 없을 수도 있다.

```text
         2           -> 노드 2는 "root node" 이면서,
       /    \           7,5 노드의 "parent node"다.
      ◣      ◢   
     5        8      -> 노드 7,5는 노드 2의 "child node" 이면서, 
    / \        \        "internal node" 다.
   ◣   ◢        ◢    
  6     7        9   -> 노드 2,6,9는 "leaf node" 다.
  
크기가 6이고, 높이가 3인 트리 구조
```

- node: 트리를 구성하고 있는 기본 요소
- edge: 노드와 노드간의 연결선 (간선)
- root node: 부모가 없는 최상위 노드
- parent node: 부모 노드, 자식 노드를 가진 노드
- child node: 자식 노드, 부모의 하위 노드
- sibling node: 형제 노드, 부모가 같은 노드
- leaf node: 잎 노드(말단 노드, terminal node), 자식 노드가 없는 노드
- internal node: 내부 노드(가지 노드, branch node)
    - 잎 노드가 아닌 노드
    - 자식 노드가 하나 이상 가진 노드
- degree: 노드의 차수, 자식의 개수
- path: 경로, 노드의 열
    - 한 노드에서 다른 한 노드에 이르는 길 사이에 놓여있는 노드들의 순서
    - 2 & 6 경로 = 2 -> 5 -> 6
- path length: 경로의 길이
    - 경로가 포함하는 방향 간선의 수이다.
    - 특히, 시작점과 출발점이 같은 경로의 길이는 0이다.
    - 2 & 6 경로의 길이 = 2 -> 5 -> 6 = 2
- depth: 깊이, 루트 노드에서 자신까지 가는 간선의 수
    - 루트 노드의 깊이는 0이다.
- level: 노드의 레벨, 루트 노드에서 자신까지 가는 경로의 길이 더하기 1이다.
    - 특히, 루트 노드의 레벨은 1이다.
    - 간혹 트리의 특정 깊이를 가지는 노드의 집합을 레벨이라고 하기도 한다.
- height: 노드의 높이, 그 노드와 단말 노드 사이의 경로의 최대 길이이다.
- width: 노드의 너비, 레벨에 있는 노드 수
- size: 노드의 크기, 자기 자신 및 모든 자손 노드의 수이다.

## 대표적인 트리 자료 구조 유형

- 자가 균형 이진 트리 탐색 트리
- Binary Tree (이진 트리)
- B-Tree
- B+Tree

## 사용 사례

- OS Directory structure
- File System
- RDBMS Index
- Data Structure
    - Heap
    - Trie
        - 문자열을 저장하고 효율적으로 탐색하기 위한 트리 형태의 자료구조

# Reference

- [wiki - tree](https://ko.wikipedia.org/wiki/%ED%8A%B8%EB%A6%AC_%EA%B5%AC%EC%A1%B0)
