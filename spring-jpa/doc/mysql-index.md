# MySQL Index 개인 정리 노트

SQL 튜닝의 핵심은 **디스크 I/O를 최소화**하는 것이다.

데이터베이스 버퍼(Buffer Pool)에서 처리할 수 있도록 쿼리 처리 과정에서 액세스하는 페이지 수를 최소화해야 한다.

## 실제 사례: 504 타임아웃 이슈

실제 운영 과정에서 애플리케이션의 성능에 미치는 대부분의 이슈들은 RDBMS 의 조회 성능에 의해 비롯된다.

예를 들어 페이지 로딩 불가, 느린 페이지 진입 등 이슈 인입과 같이 단순한 느린 조회에서 끝나는 게 아닌 그만큼 점유 대상이 된다.

- 파트너 관리자 결제 내역 페이지 로딩 불가 (504 Timeout)
- 원인: 느린 조회 쿼리 (약 48초 소요)
- SQL 실행 시 과도한 디스크 I/O 발생

**핵심 인사이트**

- Disk I/O = 쿼리 속도의 절대적 요소
- 튜닝의 첫 단계는 '쿼리 I/O 줄이기' = 인덱스 설계

**개선 결과**

- 48초 → 124ms (약 387배 개선)
- 커버링 인덱스 + 스칼라 서브쿼리 + no-offset 페이지네이션 적용

## Page와 Buffer Pool

MySQL에서 페이지(Page)는 InnoDB 스토리지 엔진이 데이터를 저장하고 관리하는 기본 단위다.

**Page의 특징**

- 기본 크기: **16KB**
- MySQL은 데이터를 **Page 단위로 읽고 쓴다**
- Row 1개를 읽어도 해당 Page 전체(16KB)를 읽음

**MySQL 아키텍처**

```
┌─────────────────────────────────────┐
│       MySQL Server                  │
│  SQL Parser → Optimizer → Executor  │
└─────────────────────────────────────┘
              ↓↑ (데이터 요청/응답)
┌─────────────────────────────────────┐
│    InnoDB Storage Engine            │
│  ┌───────────────────────────────┐  │
│  │  Buffer Pool (메모리 캐시)       │  │
│  │  - 자주 사용하는 데이터 캐싱        │  │
│  └───────────────────────────────┘  │
│            ↓↑ (Disk I/O)            │
│  ┌───────────────────────────────┐  │
│  │  Disk (데이터 파일)              │  │
│  │  - 실제 데이터 저장               │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

**쿼리 실행 시 데이터 읽기**

```
쿼리 실행 → Buffer Pool 확인
           ├─ Hit (메모리에 있음) → 즉시 반환 (빠름, 나노초)
           └─ Miss (메모리에 없음) → Disk에서 읽기 (느림, 밀리초)
```

**Page 종류**

- **Data Pages**: 실제 테이블의 행 데이터 저장
- **Index Pages**: 인덱스 데이터 저장
- **Undo/Redo Log Pages**: 트랜잭션 정보 저장

## B+Tree 구조

대부분 RDBMS의 인덱스 구조는 B-tree 기반으로 동작한다.

MySQL InnoDB는 시퀀셜 접근과 메모리 효율성을 고려하여, B-tree를 확장한 **B+tree** 자료 구조 기반으로 인덱스를 구성한다.

### B+Tree의 핵심 특징

**1. 정렬된 데이터 구조**

- 인덱스는 왼쪽부터 오름차순(ASC)으로 정렬되어 저장
- 정렬된 인덱스를 사용하여 검색 범위를 빠르게 좁힐 수 있음
- 범위 검색 시 시작점을 찾고 순차적으로 읽기만 하면 됨

**2. 리프 노드의 연결 리스트 (핵심!)**

- B+tree는 리프 노드간 **양방향 연결 리스트**로 연결
- Range Query 최적화: 시작 노드부터 순차 스캔 가능
- ORDER BY 최적화: 정렬 작업 불필요
- Sequential I/O: 캐시 친화적

**3. 높이가 낮음 (메모리 효율)**

- B+tree는 리프 노드에만 키와 데이터(PK)를 저장
- 내부 노드는 키만 관리 → 한 노드에 더 많은 키 저장 가능
- 트리 깊이 감소 → 디스크 I/O 횟수 감소
- 예: 높이가 3이면 최대 3번의 디스크 I/O로 검색 완료

**4. 노드 당 많은 키**

- 한 번의 디스크 I/O로 많은 키를 읽을 수 있음
- 메모리 효율성과 캐시 히트율 향상

### B+Tree 구조 예시

```
Root: 50, 100
                      ┌─────────────┐
                      │   50 | 100  │  (Root Page - 16KB)
                      └─────────────┘
                       /      |      \
            ┌─────────┘   ┌───┘       └─────────┐
            │             │                     │
            ▼             ▼                     ▼
      ┌─────────┐   ┌─────────┐         ┌─────────┐
      │ 10 | 30 │   │ 60 | 80 │         │110 | 130│  (Internal Nodes)
      └─────────┘   └─────────┘         └─────────┘
       /   |   \     /   |   \           /    |     \

Leaf Nodes (연결 리스트 - 핵심!)
┌──────────┐◄─►┌──────────┐◄─►┌──────────┐◄─►┌──────────┐◄─►┌──────────┐◄─►┌──────────┐
│ 5,10,15  │   │ 30,35,40 │   │ 50,55    │   │ 60,65,70 │   │ 80,85,90 │   │110,120   │
│ [PK Ptr] │   │ [PK Ptr] │   │ [PK Ptr] │   │ [PK Ptr] │   │ [PK Ptr] │   │ [PK Ptr] │
└──────────┘   └──────────┘   └──────────┘   └──────────┘   └──────────┘   └──────────┘
    ↕              ↕              ↕              ↕              ↕              ↕
prev|next      prev|next      prev|next      prev|next      prev|next      prev|next
```

### 검색 동작 예시: Key = 55

**Page 단위 검색 과정 (Disk I/O 카운트)**

```
Step 1: Root Page 읽기 (Disk I/O #1)
┌────────────────────┐
│  Keys: [50, 100]   │
└────────────────────┘
55 > 50 && 55 < 100 → 중간 포인터 선택

Step 2: Internal Node 읽기 (Disk I/O #2)
┌────────────────────┐
│  Keys: [60, 80]    │
└────────────────────┘
55 < 60 → 왼쪽 포인터 선택

Step 3: Leaf Node 읽기 (Disk I/O #3)
┌────────────────────┐
│  Keys: [50, 55]    │
│  PK: [R50, R55] ✓  │
└────────────────────┘
찾음! → PK(R55)로 실제 데이터 페이지 접근 (Disk I/O #4)

총 Disk I/O: 4번 (인덱스 3번 + 데이터 1번)
```

### Range 검색 예시: 50 ~ 90

**연결 리스트 활용**

```
Step 1-2: Root → Internal 탐색 (2 Disk I/O)
Step 3: 첫 번째 Leaf 찾기 (1 Disk I/O)

┌──────────┐      ┌──────────┐      ┌──────────┐
│ 50, 55   │ next │ 60,65,70 │ next │ 80,85,90 │
│  ✓ 수집   │─────→│  ✓ 수집   │─────→│  ✓ 수집    │
└──────────┘      └──────────┘      └──────────┘

총 Disk I/O: 5번 (Root 1 + Internal 1 + Leaf 3)
연결 리스트 덕분에 순차 스캔 가능!
```

### 클러스터링 키(PK)의 역할

**인덱스 검색 프로세스**

1. 인덱스(B+Tree) 탐색 → 조건에 맞는 리프 노드 찾기
2. 리프 노드에서 **PK(클러스터링 키)** 추출
3. **PK를 이용해 실제 테이블 데이터 페이지에서 Row 조회** (Random I/O 발생)

**핵심 포인트**

- 리프 노드에 저장된 PK = 실제 테이블 Row의 위치
- PK로 데이터를 가져올 때 **Random I/O** 발생
- Random I/O가 많을수록 성능 저하 → 커버링 인덱스로 해결

### 클러스터링 인덱스 vs 세컨더리 인덱스

**1. 클러스터링 인덱스 (Clustered Index) = PK 인덱스**

```
구조:
[Root/Branch Nodes]
        ↓
[Leaf Nodes] ← 실제 테이블 데이터가 여기에 저장됨!
  (PK, 모든 컬럼 데이터)
```

**특징**

- **테이블당 1개만 존재** (PK 기준)
- 리프 노드에 **실제 테이블의 모든 데이터**가 저장됨
- PK 순서대로 물리적으로 정렬되어 저장
- PK로 조회 시 **매우 빠름** (인덱스 조회 = 데이터 조회)

```sql
-- PK로 조회 시: 클러스터링 인덱스만 탐색
SELECT *
FROM tb_payment
WHERE id = 100;
-- 1단계: 클러스터링 인덱스(PK)에서 id=100 찾기
-- 2단계: 리프 노드에 모든 데이터가 있음 → 즉시 반환 ✅
```

**2. 세컨더리 인덱스 (Secondary Index) = 일반 인덱스**

```
구조:
[Root/Branch Nodes]
        ↓
[Leaf Nodes] ← PK 값만 저장됨
  (인덱스 컬럼 값, PK)
```

**특징**

- 테이블당 **여러 개 생성 가능**
- 리프 노드에 **인덱스 컬럼 값 + PK**만 저장
- 실제 데이터를 가져오려면 **PK로 클러스터링 인덱스를 다시 조회**해야 함

```sql
-- 세컨더리 인덱스로 조회 시: 2단계 조회
CREATE INDEX idx_company_name ON tb_payment (company_name);

SELECT *
FROM tb_payment
WHERE company_name LIKE '알서포트%';
-- 1단계: 세컨더리 인덱스(idx_company_name)에서 '알서포트'로 시작하는 PK 목록 추출
--        예: [101, 523, 1247, ..., 98234]
-- 2단계: 추출한 PK로 클러스터링 인덱스에서 실제 데이터 조회 (Random I/O 발생) ⚠️
```

## EXPLAIN으로 실행 계획 확인하기

쿼리 튜닝의 첫 단계는 **EXPLAIN으로 실행 계획 확인**이다.

### 기본 사용법

```sql
EXPLAIN
SELECT *
FROM tb_user
WHERE name = 'gmoon';
```

### 주요 컬럼 해석

**1. type - 접근 방식 (성능 순서: 위에서 아래로)**

| type   | 설명                          | 성능        |
|--------|-----------------------------|-----------|
| const  | PK나 유니크 키로 단일 row 조회        | ⭐⭐⭐ 가장 빠름 |
| eq_ref | JOIN 시 PK나 유니크 키로 단일 row 매칭 | ⭐⭐⭐ 매우 좋음 |
| ref    | 인덱스로 여러 row 조회              | ⭐⭐ 좋음     |
| range  | 인덱스 범위 스캔 (>, <, BETWEEN)   | ⭐ 괜찮음     |
| index  | 인덱스 전체 스캔                   | ⚠️ 느림     |
| ALL    | 테이블 전체 스캔 (Full Scan)       | ❌ 가장 느림   |

**2. key - 실제 사용된 인덱스**

- `NULL`: 인덱스 미사용 → **문제 발생** ⚠️
- `idx_name`: idx_name 인덱스 사용 중

**3. rows - 옵티마이저가 예상하는 검색 row 수**

- 작을수록 좋음
- 실제 결과와 차이가 크면 통계 정보 갱신 필요

**4. Extra - 추가 정보**

- `Using index`: **커버링 인덱스** 사용 → 최고의 성능 ✅
- `Using where`: WHERE 절 필터링
- `Using filesort`: 정렬 작업 발생 → ORDER BY 최적화 필요 ⚠️
- `Using temporary`: 임시 테이블 사용 → GROUP BY 최적화 필요 ⚠️

### 실제 예시: 느린 쿼리 진단

**문제 쿼리**

```sql
EXPLAIN
SELECT *
FROM tb_user
WHERE name LIKE 'gmoon%';
```

**개선 전 (인덱스 없음)**

```
+----+-------------+---------+------+---------------+------+---------+------+---------+-------------+
| id | select_type | table   | type | possible_keys | key  | key_len | ref  | rows    | Extra       |
+----+-------------+---------+------+---------------+------+---------+------+---------+-------------+
|  1 | SIMPLE      | tb_user | ALL  | NULL          | NULL | NULL    | NULL | 1000000 | Using where |
+----+-------------+---------+------+---------------+------+---------+------+---------+-------------+

문제점:
- type: ALL → Full Scan (❌ 최악)
- key: NULL → 인덱스 미사용 (❌)
- rows: 1000000 → 100만 건 전체 읽음 (❌)
성능: 약 5초
```

**개선 후 (인덱스 추가)**

```sql
CREATE INDEX idx_name ON tb_user (name);
EXPLAIN
SELECT *
FROM tb_user
WHERE name LIKE 'gmoon%';
```

```
+----+-------------+---------+-------+---------------+----------+---------+------+------+-----------------------+
| id | select_type | table   | type  | possible_keys | key      | key_len | ref  | rows | Extra                 |
+----+-------------+---------+-------+---------------+----------+---------+------+------+-----------------------+
|  1 | SIMPLE      | tb_user | range | idx_name      | idx_name | 767     | NULL |    4 | Using index condition |
+----+-------------+---------+-------+---------------+----------+---------+------+------+-----------------------+

개선 포인트:
- type: range → 인덱스 범위 스캔 (✅)
- key: idx_name → 인덱스 사용 (✅)
- rows: 4 → 100만 건 → 4건으로 대폭 감소 (✅)
성능: 약 0.01초 (500배 개선!)
```

## 비용 기반 옵티마이저 (CBO)

쿼리 실행 계획의 인덱스는 **옵티마이저가 선정**한다.

MySQL 8은 **비용 기반 옵티마이저(Cost-Based Optimizer, CBO)** 기반으로 동작한다.

### CBO 동작 방식

1. 사용자 쿼리 입력
2. 옵티마이저가 후보 실행 계획 도출
3. **통계 정보 기반**으로 각 실행 계획의 예상 비용 계산
4. **가장 낮은 비용의 실행 계획** 선택
5. 쿼리 실행

### CBO가 사용하는 통계 정보

- 데이터량 (총 row 수)
- 컬럼 값의 수 (카디널리티)
- 컬럼 값 분포
- 인덱스 높이
- 클러스터링 팩터

### 통계 정보 관리

**통계 정보가 부정확하면?**

- CBO는 통계 정보 기반으로 비용 계산
- 통계가 부정확하면 **잘못된 인덱스 선택** 가능
- 예상과 다른 실행 계획으로 슬로우 쿼리 발생

**ANALYZE TABLE로 통계 정보 갱신**

```sql
ANALYZE TABLE tb_user;
```

**언제 실행해야 하나?**

- 대량 데이터 INSERT/DELETE 후
- 인덱스 추가/삭제 후
- 쿼리 성능이 갑자기 느려졌을 때

**주의사항**

- ⚠️ **Read Lock 발생**: 트래픽이 적은 새벽 시간대 권장
- 대용량 테이블은 실행 시간이 길 수 있음

## 인덱스 컬럼 선정 기준

**기본 원칙**

- 갱신이 자주되지 않는 컬럼인가?
- 분포도(selectivity)가 좋은 컬럼인가?
    - 분포도는 낮을 수록 좋다 (유니크할 수록 좋다)
- 조인절에 연결되어 있는 컬럼인가?
- 정렬에 사용되는 컬럼인가?

### 선택도(Selectivity) 계산

```sql
-- name 컬럼 값 분포도
SELECT COUNT(DISTINCT name) / COUNT(*) * 100 AS index_selectivity
FROM tb_user;
```

**분포도 = (컬럼 고유 값 ROW 수) / (테이블의 총 ROW 수) * 100**

- **80-90% 이상**: 매우 좋음 ✅
    - 인덱스가 대부분의 값들에 대해 고르게 분포
    - 쿼리 성능 최적화
- **60-80%**: 여전히 괜찮음 👍
    - 대부분의 쿼리가 효율적으로 수행
    - 특정 경우 성능 저하 가능성
- **50-60% 이하**: 인덱스 효율성 저하 ⚠️
    - 특정 값에 데이터 집중
    - 쿼리 성능에 부정적 영향

## 복합 인덱스 (Composite Index)

복합 인덱스는 2개 이상의 컬럼을 조합한 인덱스다. **컬럼 순서가 매우 중요**하다.

### 복합 인덱스 핵심 원칙

**1. 선행 컬럼부터 순차적으로 사용 가능**

전화번호부를 생각해보자. 전화번호부는 (성 → 이름) 순으로 정렬되어 있다.

```sql
-- 복합 인덱스 생성
CREATE INDEX idx_name_status ON tb_user (name, status);

-- 인덱스 사용 가능 케이스
WHERE name = 'gmoon'                    -- ✅ name만 사용
WHERE name = 'gmoon' AND status = 'ACTIVE'  -- ✅ name + status 사용

-- 인덱스 사용 불가 케이스
WHERE status = 'ACTIVE'                 -- ❌ 선행 컬럼(name) 없음
```

**2. 범위 조건 이후 컬럼은 인덱스 사용 불가**

```sql
CREATE INDEX idx_name_created_status ON tb_user (name, created_at, status);

WHERE name = 'gmoon'                    -- ✅ name 사용
  AND created_at > '2024-01-01'         -- ✅ 범위 검색까지 사용
  AND status = 'ACTIVE'                 -- ❌ 범위 조건 이후라 사용 불가
```

**왜 이런 현상이 발생할까?**

복합 인덱스는 **왼쪽 컬럼부터 순차적으로 정렬**되어 있기 때문

```
인덱스 정렬 순서: (name, status, created_at)

1. name 기준 오름차순 정렬
2. 같은 name 내에서 status 오름차순 정렬
3. 같은 name, status 내에서 created_at 오름차순 정렬

예시:
('gmoon', 'ACTIVE', '2024-01-01')
('gmoon', 'ACTIVE', '2024-01-02')
('gmoon', 'INACTIVE', '2024-01-01')
('hongkd', 'ACTIVE', '2024-01-01')
```

### 복합 인덱스 설계 순서

**1순위: 동등 비교 조건 (=)**

- 가장 앞에 배치
- 선택도가 높은 컬럼 우선

**2순위: 정렬 조건 (ORDER BY)**

- 동등 조건 다음에 배치
- Filesort 제거 가능

**3순위: 범위 조건 (>, <, BETWEEN)**

- 가장 뒤에 배치
- 범위 조건 이후는 인덱스 사용 불가

**예시**

```sql
-- 쿼리
SELECT *
FROM orders
WHERE status = 'PENDING'
ORDER BY created_at DESC
LIMIT 10;

-- 최적 인덱스
CREATE INDEX idx_status_created ON orders (status, created_at DESC);
```

## 커버링 인덱스 (Covering Index)

**커버링 인덱스 = 인덱스만으로 쿼리를 완전히 처리 가능한 상태**

### 일반 인덱스 vs 커버링 인덱스

**일반 인덱스**

```sql
CREATE INDEX idx_name ON tb_user (name);
SELECT name, email
FROM tb_user
WHERE name = 'gmoon';

1. 인덱스에서 name = 'gmoon' 검색 → PK 찾기
2. PK로 테이블 데이터 페이지 접근 → email 조회 (Random I/O 발생)
```

**커버링 인덱스**

```sql
CREATE INDEX idx_name_email ON tb_user (name, email);
SELECT name, email
FROM tb_user
WHERE name = 'gmoon';

1. 인덱스에서 name = 'gmoon' 검색
2. 인덱스에 email도 포함되어 있음
3. 테이블 접근 불필요! (Random I/O 제거)

EXPLAIN에서 확인: Extra: Using index
```

### 커버링 인덱스 활용 전략

**전략 1: SELECT 절 최소화**

```sql
-- Bad: 모든 컬럼 조회
SELECT *
FROM orders
WHERE status = 'PENDING';

-- Good: 필요한 컬럼만 조회
SELECT id, status, created_at
FROM orders
WHERE status = 'PENDING';
-- 인덱스: idx_status_created_id (status, created_at, id)
```

**전략 2: 복합 인덱스 설계 시 SELECT 절 고려**

```sql
-- 자주 사용되는 쿼리
SELECT id, name, status
FROM tb_user
WHERE name = 'gmoon'
  AND status = 'ACTIVE';

-- 커버링 인덱스 설계
CREATE INDEX idx_name_status_id ON tb_user (name, status, id);
-- id는 PK지만 명시적으로 포함하여 커버링 인덱스 구성
```

### 커버링 인덱스 효과

```sql
-- Before: 일반 인덱스 (48초)
idx_name
    (name)

-- After: 커버링 인덱스 (124ms)
    idx_name_status_created_id
    (name, status, created_at, id)

    성능 개선: 48초 → 124ms (387배 개선)
핵심: Random I/O 완전 제거
```

## 정렬(ORDER BY)과 인덱스

**인덱스를 활용한 정렬**

- 인덱스는 이미 정렬된 상태로 저장됨
- ORDER BY 컬럼이 인덱스 순서와 일치하면 정렬 작업 생략 가능

```sql
CREATE INDEX idx_created ON tb_user (created_at);
SELECT *
FROM tb_user
ORDER BY created_at;
-- 인덱스 순서 그대로 읽으면 되므로 추가 정렬 불필요
```

**Filesort 발생 조건**

- ORDER BY 컬럼이 인덱스에 없는 경우
- WHERE 조건과 ORDER BY 컬럼의 인덱스 순서가 다른 경우
- Filesort = 메모리 또는 디스크에서 별도 정렬 작업 수행 → 느림

**복합 인덱스와 ORDER BY**

```sql
-- 인덱스: (status, created_at)
CREATE INDEX idx_status_date ON tb_user (status, created_at);

-- ✅ 인덱스 활용 (Filesort 없음)
WHERE status = 'ACTIVE' ORDER BY created_at

-- ❌ Filesort 발생
WHERE status = 'ACTIVE' ORDER BY name

-- ❌ Filesort 발생 (인덱스 순서와 다름)
ORDER BY created_at, status
```

## 실전 튜닝 사례

### 관리자 페이지 48초 → 124ms 개선

**개선된 쿼리**

```sql
-- 관리자 페이지 조회: 48s → 124ms (387배 개선)
EXPLAIN
SELECT team0_.id,
       team0_.type,
       team0_.status,
       -- 스칼라 서브 쿼리 적용 (JOIN 제거로 성능 개선)
       (SELECT user2_.name
        FROM tb_user user2_
        WHERE user2_.company_id = company0_.id
          AND role = 'ADMIN') AS col_12_0_
FROM tb_team team0_
         INNER JOIN tb_company company0_ ON team0_.company_id = company0_.id
WHERE
  -- Loose Index Scan 설정 (강제로 인덱스 타게 하기)
    (team0_.created_dt BETWEEN '1900-01-01' AND '2099-01-01')
  -- 커버링 인덱스 적용
  AND (team0_.company_id IN (SELECT company4_.id
                             FROM tb_company company4_
                             WHERE (company4_.name LIKE 'gmoon%')))
ORDER BY team0_.created_dt DESC
LIMIT 15 -- no-offset 페이지네이션
```

### 핵심 개선 포인트

**1. 튜닝 전/후 실행 계획과 인덱스를 반드시 확인**

- EXPLAIN으로 type, key, rows 확인
- 개선 전후 비교로 효과 검증

**2. 범위 조건보다 동등 조건(=) 권장**

- `WHERE id = 100` ✅ (const)
- `WHERE id > 100` ⚠️ (range, 이후 컬럼 인덱스 사용 불가)

**3. 불필요한 JOIN 제거 검토**

- JOIN vs 스칼라 서브 쿼리 성능 비교 필요
- 데이터 양에 따라 스칼라 서브 쿼리가 더 빠를 수 있음
- 특히 1:N 관계에서 JOIN 시 중복 데이터 발생 → 불필요한 I/O

**4. 부정문보다는 긍정문 사용**

- `NOT IN` → `IN` 또는 `EXISTS`로 변경
- 옵티마이저가 긍정문으로 변환하여 실행하므로 처음부터 긍정문 사용

**5. LIKE 문자열 검색 조건 확인**

- ✅ `WHERE name LIKE 'gmoon%'` → Index Scan
- ❌ `WHERE name LIKE '%gmoon%'` → Full Scan

**6. 커버링 인덱스 적용**

- SELECT 절의 모든 컬럼을 인덱스에 포함
- Extra: Using index 확인

**7. no-offset 페이지네이션**

- `LIMIT 15 OFFSET 10000` ❌ (10000개를 읽고 버림)
- Cursor 기반 페이지네이션 ✅

**8. 인덱스 무효화 조건 피하기**

```sql
-- ❌ 나쁜 예시
WHERE DATE(created_at) = '2024-01-01'  -- 함수 사용
WHERE name + 1 = 100                   -- 연산 사용
WHERE name LIKE '%검색어%'             -- 중간 와일드카드

-- ✅ 좋은 예시
WHERE created_at >= '2024-01-01' AND created_at < '2024-01-02'
WHERE name = 99
WHERE name LIKE '검색어%'              -- 앞 고정 와일드카드
```

**9. 인덱스 최적화 명령**

```sql
-- 인덱스 재구성 (단편화 제거)
OPTIMIZE TABLE table_name;

-- 테이블 통계 업데이트
ANALYZE TABLE table_name;
```

**10. WHERE IN 절 최적화**

IN 절의 값 개수에 따라 실행 계획이 달라질 수 있음

```sql
-- IN 절은 equality range로 최적화됨
WHERE id IN ('id1', 'id2', 'id3', 'id4', 'id5')
```

MySQL 옵티마이저 동작 방식:

- IN 절 값이 **적을 때**: Index Dive로 정확한 통계 수집
- IN 절 값이 **많을 때**: Index Statistics로 빠른 추정
- 전환점: `eq_range_index_dive_limit` 설정값 (기본 200)

**11. 카디널리티 예외 전략: IN 절을 Boolean으로 변환**

일반적으로 카디널리티가 높은 컬럼을 복합 인덱스 앞에 배치하는 것이 원칙이지만, **IN 절을 Boolean 컬럼으로 변환하여 복합 인덱스 중간에 배치**하면 성능을 크게 개선할 수 있음

**문제 상황: IN 절 사용**

```sql
-- 사용자 유형이 20개 존재하는 경우
CREATE INDEX idx_user_date ON users (user_id, created_at);

-- 쿼리: 무료 라이센스 사용자만 조회 (전체의 5-10%)
SELECT *
FROM users
WHERE user_id = 100
  AND user_type IN ('FREE', 'TRIAL', 'BASIC') -- Binary Search O(log 3)
  AND created_at > '2024-01-01';
```

**해결 전략: Boolean Discriminator Column 추가**

```sql
-- 1. Boolean 컬럼 추가
ALTER TABLE users
    ADD COLUMN is_free_license_user BOOLEAN
        GENERATED ALWAYS AS (user_type IN ('FREE', 'TRIAL', 'BASIC')) STORED;

-- 2. 복합 인덱스 재구성 (Boolean 컬럼을 중간에 배치)
CREATE INDEX idx_user_license_date ON users (user_id, is_free_license_user, created_at);

-- 3. 개선된 쿼리
SELECT *
FROM users
WHERE user_id = 100
  AND is_free_license_user = true -- Equality O(1) - 빠름!
  AND created_at > '2024-01-01';
```

**성능 비교**

```sql
-- ❌ IN 절 방식: Binary Search O(log n)
WHERE user_type IN ('FREE', 'TRIAL', 'BASIC')

-- ✅ Boolean 방식: Equality O(1) - 더 빠름
WHERE is_free_license_user = true
```

**왜 효과적인가?**

- ✅ **IN 절보다 빠름**: Binary Search O(log n) → Equality O(1)
- ✅ **복합 인덱스 전체 활용**: (user_id, is_free_license_user, created_at) 모두 사용
- ✅ **Range Scan 가능**: created_at 범위 조건도 인덱스 활용
- ✅ **데이터 분포 편향 활용**: is_free_license_user = true가 5-10%일 때 특히 효과적

**주의사항**

- 데이터 분포가 **편향된 경우**에만 유효 (예: 5-10% vs 90-95%)
- Boolean 값이 균등하면 (50% vs 50%) 효과 제한적

## 핵심 체크리스트

**1. 인덱스 무효화 조건 피하기**

- WHERE 절에 함수나 연산 사용 금지
- LIKE 패턴은 앞쪽 고정만 사용

**2. 복합 인덱스 설계 원칙**

- 동등 조건(=)을 앞에, 범위 조건(>, <)을 뒤에
- 자주 조회되는 컬럼 우선
- 선택도 높은 컬럼 우선 (단, 쿼리 패턴이 더 중요)

**3. 커버링 인덱스 적극 활용**

- SELECT 절에 필요한 컬럼을 인덱스에 포함
- Random I/O 완전 제거 → 최고 성능

**4. 정렬 최적화**

- ORDER BY 컬럼을 인덱스에 포함
- WHERE + ORDER BY 조합 시 인덱스 순서 고려

**5. 항상 EXPLAIN으로 검증**

- 개발 후 반드시 EXPLAIN 확인
- type, key, rows, Extra 체크
- 특히 배포 전 부하 테스트와 함께 검증

**6. 실제 개선 프로세스**

1. 느린 쿼리 로그 확인 (slow query log)
2. EXPLAIN으로 실행 계획 분석
3. 인덱스 추가 또는 수정
4. 쿼리 재작성 (필요 시)
5. EXPLAIN으로 개선 확인
6. 부하 테스트로 검증

## 마무리

테이블에 데이터가 많을수록 인덱스 저장 비용도 고려해야 한다.

MySQL InnoDB 스토리지 엔진의 인덱스 구조는 B+Tree로 되어 있기 때문에, 트리의 깊이가 깊을수록 별도의 저장 비용이 든다. 특히 Replication DB 구조라면 불필요하게 추가된 인덱스는 복제
지연을 유발할 수 있다.

인덱스 최적화에 있어 데이터 분포도를 따져 신중히 선정해야 하고, 조회 성능을 개선한다고 무작정 인덱스를 설정하는 것은 답이 아니다.

MySQL 8 옵티마이저는 CBO 기반으로 동작하고 있기 때문에, JOIN 절의 테이블의 데이터 양과 인덱스 구성에 따라 실행 계획이 예상과 다르게 슬로우 쿼리가 발생할 수 있다. 인덱스 추가 후 실행 계획을
기반으로 조회 성능을 비교 검증하는 습관을 가져야 한다.

## Reference

- MySQL 8.0
    - [innodb file space](https://dev.mysql.com/doc/refman/8.0/en/innodb-file-space.html)
    - [B-tree](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_b_tree)
    - [Query Optimizations](https://mariadb.com/kb/ko/query-optimizations/)
    - [optimization-and-indexes](https://mariadb.com/kb/ko/optimization-and-indexes/)
- [MySQL Range Optimization](https://dev.mysql.com/doc/refman/8.0/en/range-optimization.html)
- [MySQL Composite Index](https://dev.mysql.com/doc/refman/8.0/en/multiple-column-indexes.html)
- [당근 기술 블로그 - Index Dive 비용 최적화](https://medium.com/daangn/index-dive-%EB%B9%84%EC%9A%A9-%EC%B5%9C%EC%A0%81%ED%99%94-1a50478f7df8)
- [d2 - 성능 향상을 위한 SQL 작성법](https://d2.naver.com/helloworld/1155)
