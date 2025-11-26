# MySQL Index

**세미나명**: 백엔드 개발자를 위한 MySQL 인덱스 기초 + 조회 성능 튜닝
**세미나 내용**: RDBMS 조회 성능을 결정하는 인덱스 구조와 동작 원리

- Single / Composite Index 설계 시 고려사항
- Range Scan, Index Scan 등 실행 방식 이해
- EXPLAIN 기반 쿼리 성능 분석
- 인덱스 최적화를 통한 실전 성능 개선 사례

**참석 대상**: RDBMS 인덱스 기반의 조회 성능 최적화에 관심 있는 분
**진행 시간**: 약 30분

---

## 목차

### 1. 왜 인덱스를 알아야 하는가 (3분)
- 백엔드 개발자와 인덱스
- 실제 성능 이슈 사례

### 2. 인덱스 기초 (8분)
- Full Scan vs Index Scan
- EXPLAIN으로 쿼리 분석하기
- 인덱스 구조: B+Tree
- 클러스터링 인덱스 vs 세컨더리 인덱스

### 3. 인덱스 설계 (10분)
- 단일 인덱스
- 복합 인덱스 설계 원칙
- 선택도(Selectivity)
- 커버링 인덱스

### 4. 옵티마이저 이해 (5분)
- 비용 기반 옵티마이저 (CBO)
- EXPLAIN 실행 계획 읽기
- 통계 정보 관리

### 5. 실전 튜닝 사례 (3분)
- 48초 → 124ms 개선 사례
- 핵심 체크리스트

### 6. Q&A (1분)

---

# MySQL Index

안녕하세요. MySQL Index 세미나 발표를 맡게 된 웹개발 1팀 문겸입니다.

백엔드 팀에서 인덱스를 설명한다고? 하시는 분들을 위해 들어가기전 체크 포인트 리스트를 한번 봐주시면 좋을 것 같습니다.

## 1. 왜 인덱스를 알아야 하는가 (3분)

### 1-1. 이런 분들을 위한 세미나입니다

**체크 포인트**

다음 중 하나라도 해당되시나요?

- ✅ 우리 팀은 JPA/ORM 기술 스택을 사용한다
- ✅ "처음엔 괜찮았는데 서비스가 느려졌어요" 라는 고객 문의를 받아본 적이 있다
- ✅ "인덱스? 그거 DBA가 하는 거 아니야?" 라고 생각한 적이 있다
- ✅ 쿼리 튜닝보다 캐싱이나 NoSQL이 먼저 떠오른다
- ✅ EXPLAIN을 본 적은 있는데... 정확히 뭘 봐야 할지 모르겠다

**발표 멘트**

"혹시 이 중에 하나라도 해당되시는 분 계신가요? (손들어보세요)
네, 사실 저도 다 해당됐습니다. (웃음)

특히 '인덱스는 DBA 영역 아니야?'라고 생각했었는데,
실제로 파트너 어드민 사이트 성능 개편을 하며 생각이 완전히 바뀌었습니다.

오늘은 제가 그때 겪었던 경험과 함께, 백엔드 개발자가 꼭
알아야 할 인덱스 핵심만 정리해서 말씀드리겠습니다.

만약 오늘 세미나 내용이 하나도 해당 안 되시는 분은...
이미 인덱스 고수시니까 나중에 커피 한잔 사드리겠습니다.
(웃음)"

### 1-2. 왜 백엔드가 인덱스를 알아야 하는가

**핵심 메시지**
- ORM/JPA 기반 개발에서도 결국 SQL은 개발자가 만든다
- **인덱스 설계에 필요한 정보를 가장 잘 아는 사람 = 백엔드 개발자**
  1. 실제 데이터 양과 증가 추세
  2. 테이블 구조와 관계
  3. 비즈니스 로직의 검색 조건
  4. 자주 사용되는 쿼리 패턴
- 인덱스 설계만 제대로 해도 10~100배 성능 개선 가능

**발표 멘트**

"오늘 이 주제를 선택한 이유는, 알서포트의 모든 제품 백엔드 기술 스택에서 JPA가 기본 스펙으로 자리 잡고 있기 때문입니다.

JPA는 쿼리를 자동으로 생성해주기 때문에 개발 속도를 크게 향상시킬 수 있습니다. 하지만 러닝커브가 높아서 잘못 사용하면 애플리케이션 성능에 치명적인 영향을 미칠 수 있습니다.

여기서 중요한 질문이 있습니다. '인덱스는 DBA가 설계하는 거 아니야?'

물론 DBA의 도움을 받을 수 있습니다. 하지만 실제로 인덱스를 제대로 설계하려면 다음을 알아야 합니다:

첫째, 실제 데이터가 얼마나 있고 얼마나 빠르게 증가하는가?
둘째, 테이블 간의 관계와 구조는 어떻게 되어 있는가?
셋째, 비즈니스 로직에서 어떤 조건으로 검색하는가?
넷째, 어떤 쿼리가 자주 실행되는가?

이 모든 것을 가장 잘 아는 사람이 누구일까요? 바로 백엔드 개발자입니다.

DBA는 데이터베이스 전문가이지만, 우리 서비스의 비즈니스 로직과 데이터 특성을 백엔드만큼 깊이 이해하기는 어렵습니다. 따라서 백엔드 개발자가 인덱스 설계에 주도적으로 참여해야 하고, 그러기 위해서는 인덱스를 제대로 이해해야 합니다.

오늘 세미나에서는 실제 사례를 통해 인덱스가 왜 중요한지, 그리고 어떻게 설계하고 튜닝해야 하는지 함께 알아보겠습니다."

### 1-3. 실제 사례: 파트너 어드민 사이트의 504 타임아웃 이슈

**문제 상황**
- 관리자 결제 내역 페이지 로딩 불가 (504 Timeout)
- 원인: 느린 조회 쿼리
- SQL 실행 시 과도한 디스크 I/O 발생

**핵심 인사이트**
- I/O = 쿼리 속도의 절대적 요소
- 튜닝의 첫 단계는 **"쿼리 I/O 줄이기" = 인덱스 설계**

**발표 멘트**

"실제로 겪었던 사례를 하나 말씀드리겠습니다.

파트너 관리자 대표정보 관리 페이지에서 504 타임아웃이 발생하여 페이지가 아예 로드되지 않는 현상이 있었습니다.
분석 결과, 특정 조회 쿼리가 너무 오래 걸려서 발생한 문제였습니다.

대부분의 애플리케이션 성능 이슈는 이처럼 느린 조회 쿼리에서 비롯됩니다.

데이터베이스 성능을 개선하는 핵심은 실행되는 쿼리가 디스크 I/O를 얼마나 줄일 수 있느냐입니다.

'인덱스는 DBA 영역 아닌가?'라고 생각하실 수 있지만,
JPA 기반 프로젝트에서는 조회 쿼리 로직과 비즈니스 요구사항을 가장 잘 아는 사람이 바로 백엔드 개발자입니다. 따라서 백엔드 개발자가 인덱스 설계에 적극 참여해야 합니다.

그렇다면 이 문제를 어떻게 해결할 수 있을까요? 실제 쿼리를 하나 보면서 진단해보겠습니다."

## 2. 인덱스 기초 (8분)

### 2-1. Full Scan vs Index Scan - 실제 느린 쿼리 예시

**시나리오**: 파트너 관리자 페이지에서 특정 회사의 미결제 내역 조회

```sql
-- 파트너 미결제 내역 조회 쿼리
SELECT *
FROM tb_payment
WHERE company_name LIKE '알서포트%'
  AND status = 'UNPAID';
```

### 2-2. EXPLAIN으로 쿼리 분석하기

```sql
EXPLAIN SELECT * FROM tb_payment
WHERE company_name LIKE '알서포트%'
  AND status = 'UNPAID';
```

**결과**
```
+----+-------------+------------+------+---------------+------+---------+------+---------+-------------+
| id | select_type | table      | type | possible_keys | key  | key_len | ref  | rows    | Extra       |
+----+-------------+------------+------+---------------+------+---------+------+---------+-------------+
|  1 | SIMPLE      | tb_payment | ALL  | NULL          | NULL | NULL    | NULL | 1000000 | Using where |
+----+-------------+------------+------+---------------+------+---------+------+---------+-------------+
```

**문제점 발견**
- `type: ALL` → 테이블 전체 스캔 (Full Scan)
- `key: NULL` → 인덱스를 사용하지 않음
- `rows: 1000000` → 100만 건 전체를 읽어야 함

### 2-3. 인덱스 구조: B+Tree 이해

**Full Scan 동작 과정**
```
1. MySQL 엔진이 쿼리 실행
   ↓
2. tb_payment 테이블의 모든 Data Page를 읽기 시작
   ↓
3. 100만 건의 결제 데이터를 순차적으로 확인
   - 각 Row의 company_name 컬럼을 검사
   - '알서포트'로 시작하는지 확인
   - status가 'UNPAID'인지 확인
   ↓
4. 조건에 맞는 Row만 반환
```

**왜 느릴까?**
- 💥 **과도한 Disk I/O**: 모든 데이터를 읽어야 함
- 💥 **데이터가 많을수록 느려짐**: 100만 건 → 1000만 건이면 10배 느려짐

**발표 멘트**

"EXPLAIN으로 확인해보니 type이 ALL이고 key가 NULL입니다. 이는 인덱스를 전혀 사용하지 않고 테이블 전체를 스캔한다는 의미입니다.

파트너 관리자가 '알서포트'로 시작하는 회사의 미결제 내역을 조회하려고 하는데, MySQL은 100만 건의 결제 데이터를 처음부터 끝까지 읽으면서 회사명과 상태를 확인해야 합니다. 당연히 느릴 수밖에 없죠.

그렇다면 왜 이렇게 많은 데이터를 읽어야 할까요? 이를 이해하려면 MySQL의 Disk I/O와 Page 개념을 알아야 합니다."

### 2-4. Full Scan이 느린 이유: Disk I/O

#### MySQL 아키텍처 (간략)

```
┌─────────────────────────────────────┐
│       MySQL Server                  │
│  SQL Parser → Optimizer → Executor  │
└─────────────────────────────────────┘
              ↓↑ (데이터 요청/응답)
┌─────────────────────────────────────┐
│    InnoDB Storage Engine            │
│  ┌───────────────────────────────┐  │
│  │  Buffer Pool (메모리 캐시)     │  │
│  │  - 자주 사용하는 데이터 캐싱   │  │
│  └───────────────────────────────┘  │
│            ↓↑ (Disk I/O)            │
│  ┌───────────────────────────────┐  │
│  │  Disk (데이터 파일)            │  │
│  │  - 실제 데이터 저장            │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

**쿼리 실행 시 데이터를 읽는 과정**
```
쿼리 실행 → Buffer Pool 확인
           ├─ Hit (메모리에 있음) → 즉시 반환 (빠름, 나노초)
           └─ Miss (메모리에 없음) → Disk에서 읽기 (느림, 밀리초)
```

#### Page = MySQL이 데이터를 읽는 기본 단위

**Page의 특징**
- 기본 크기: **16KB**
- MySQL은 데이터를 **Page 단위로 읽고 쓴다**
- Row 1개를 읽어도 해당 Page 전체(16KB)를 읽음

**Page의 종류**
- **Data Pages**: 실제 테이블의 행 데이터 저장
- **Index Pages**: 인덱스 데이터 저장
- **Undo/Redo Log Pages**: 트랜잭션 정보 저장

**Buffer Pool = Page 캐싱 영역**
- 자주 사용되는 Page를 메모리에 캐싱
- 물리 메모리의 50-80%를 Buffer Pool로 할당
- Buffer Pool에 있으면 빠르고, 없으면 Disk I/O 발생

**발표 멘트**

"MySQL은 크게 두 계층으로 나뉩니다. MySQL Server는 쿼리를 분석하고 최적화하며, Storage Engine은 실제 데이터를 읽고 씁니다.

여기서 핵심은 Buffer Pool입니다. 자주 사용되는 데이터를 메모리에 캐싱해서 Disk I/O를 최소화하는 거죠.

메모리에 데이터가 있으면 즉시 반환할 수 있지만, 없으면 디스크에서 읽어야 하므로 수천 배 느려집니다.

그리고 MySQL은 데이터를 개별 Row 단위가 아니라 16KB 크기의 Page 단위로 읽습니다. 따라서 Full Scan을 하면 수백~수천 개의 Page를 읽어야 하므로 엄청난 Disk I/O가 발생하는 겁니다.

이 문제를 해결하려면 어떻게 해야 할까요? 바로 인덱스를 추가하는 것입니다."

### 2-5. 인덱스란 무엇인가?

**간단히 말하면: 책의 색인(목차)**

책에서 특정 내용을 찾을 때
- ❌ 나쁜 방법: 1페이지부터 끝까지 전부 읽기
- ✅ 좋은 방법: 뒤쪽 색인에서 키워드 찾고 → 해당 페이지로 바로 이동

데이터베이스도 마찬가지
- ❌ Full Scan: 테이블 전체를 처음부터 끝까지 읽기
- ✅ Index Scan: 인덱스에서 위치 찾고 → 해당 데이터로 바로 이동

### 2-6. 단일 인덱스로 문제 해결

```sql
-- company_name 컬럼에 인덱스 생성
CREATE INDEX idx_company_name ON tb_payment(company_name);
```

**단일 인덱스 생성**

```sql
EXPLAIN SELECT * FROM tb_payment
WHERE company_name LIKE '알서포트%'
  AND status = 'UNPAID';
```

**개선 결과**
```
+----+-------------+------------+-------+------------------+------------------+---------+------+------+-----------------------+
| id | select_type | table      | type  | possible_keys    | key              | key_len | ref  | rows | Extra                 |
+----+-------------+------------+-------+------------------+------------------+---------+------+------+-----------------------+
|  1 | SIMPLE      | tb_payment | range | idx_company_name | idx_company_name | 767     | NULL |   15 | Using index condition |
+----+-------------+------------+-------+------------------+------------------+---------+------+------+-----------------------+
```

**개선 포인트**
- `type: range` → 인덱스 범위 스캔 사용 ✅
- `key: idx_company_name` → 인덱스 사용 중 ✅
- `rows: 15` → 100만 건 → 15건으로 대폭 감소 ✅

**성능 개선**
- **검색 시간: 5초 → 0.02초 (250배 개선)**
- **읽은 Page 수: 수천 개 → 수십 개**

### 2-7. 인덱스 동작 원리: Full Scan vs Index Scan

#### Full Scan vs Index Scan 비교

**케이스 1: 인덱스가 없을 때 (Full Scan)**

```
1. MySQL 엔진이 쿼리 실행
   ↓
2. Buffer Pool에서 tb_payment의 모든 Data Page 확인
   - Buffer Pool에 없는 Page는 Disk에서 로드 (Disk I/O 발생)
   ↓
3. 모든 Page를 순차적으로 읽으며 company_name, status 컬럼 확인
   - 100만 건이면 100만 건 모두 확인
   - 수백~수천 개의 Page 읽기 필요
   ↓
4. 조건에 맞는 Row만 반환
```

**문제점**
- 💥 **과도한 Disk I/O**: 모든 Data Page를 읽어야 함
- 💥 **Random I/O 발생**: Page가 디스크 곳곳에 흩어져 있음
- 💥 **데이터 증가 시 성능 급격히 악화**: 100만 건 → 1000만 건이면 10배 느려짐

**케이스 2: 인덱스가 있을 때 (Index Scan)**

```
1. MySQL 엔진이 쿼리 실행
   ↓
2. company_name 컬럼의 인덱스(B+Tree)를 탐색
   - Buffer Pool에 인덱스 Page 로드 (크기가 작아 빠름)
   - '알서포트'로 시작하는 범위를 빠르게 탐색
   ↓
3. 리프 노드에서 조건에 맞는 PK 목록 추출
   - 예: [101, 523, 1247, ..., 98234] (15건만 찾음)
   ↓
4. PK로 실제 테이블 Data Page에서 Row 조회
   - status = 'UNPAID' 조건 추가 확인
   - 15개의 Page만 읽으면 됨 (Random I/O 최소화)
   ↓
5. 조건에 맞는 Row 반환
```

**장점**
- ✅ **최소 Disk I/O**: 필요한 Page만 선택적으로 읽음
- ✅ **Random I/O 최소화**: 인덱스로 필터링 후 필요한 것만 접근
- ✅ **데이터 증가해도 성능 안정적**: B+Tree는 로그 시간 복잡도 O(log n)

[그림: Buffer Pool과 Disk I/O 상세 흐름도]
```
[Buffer Pool (Memory)]           [Disk]
┌─────────────────┐             ┌─────────────────┐
│ Index Pages     │  ←읽기→      │ Index Files     │
│ (작고 빠름)       │             │                 │
├─────────────────┤             ├─────────────────┤
│ Data Pages      │  ←읽기→      │ Data Files      │
│ (필요한 것만)      │             │ (Random I/O)    │
└─────────────────┘             └─────────────────┘
```

**발표 멘트**

"company_name에 인덱스를 추가하니 type이 range로 바뀌고, key에 idx_company_name이 표시됩니다. 그리고 rows가 100만에서 15로 줄었습니다. 성능은 250배 개선되었죠.

왜 이렇게 빠를까요?

인덱스가 없으면 MySQL은 테이블 전체를 스캔해야 합니다. 100만 건의 결제 데이터가 저장된 수천 개의 Page를 모두 읽어야 하죠.

하지만 인덱스가 있으면 다릅니다. 먼저 인덱스를 탐색해서 '알서포트'로 시작하는 회사의 결제 데이터 위치를 빠르게 찾습니다. 그리고 해당 위치의 데이터만 읽으면 되므로 극소수의 Page만 접근하면 됩니다. status 조건은 테이블에서 추가로 확인하지만, 이미 15건으로 줄어든 상태라 부담이 적습니다.

그렇다면 인덱스는 어떤 구조로 되어 있길래 이렇게 빠를까요?"

### 2-8. B+Tree 구조 상세

**B+Tree의 특징**

**구조**
- 대부분의 RDBMS는 B-Tree 기반 인덱스 사용
- MySQL InnoDB는 B+Tree 자료구조 사용 (B-Tree의 확장 버전)
- 시퀀셜 접근과 메모리 효율성을 고려한 구조

**B+Tree의 구성**
- **루트/브랜치 노드**: 키 값만 저장 (탐색 경로 제공)
- **리프 노드**: 실제 인덱스 값 + PK(클러스터링 키) 저장
- **리프 노드는 Linked List로 연결** → 범위 검색에 최적화

[그림: B+Tree 구조 예시]
```
                    [Root Node]
                   /     |     \
          [Branch]   [Branch]   [Branch]
          /    \      /    \      /    \
    [Leaf] [Leaf] [Leaf] [Leaf] [Leaf] [Leaf]
     ↔       ↔       ↔       ↔       ↔       ↔  (Linked List)
   (값,PK) (값,PK) (값,PK) (값,PK) (값,PK) (값,PK)
```

#### 인덱스의 핵심 특성: 정렬

**인덱스는 기본적으로 왼쪽부터 오름차순(ASC)으로 정렬되어 저장된다**

```sql
-- 예: created_at 컬럼 인덱스
-- 리프 노드 저장 순서
[2024-01-01] → [2024-01-02] → [2024-01-03] → ... (왼쪽에서 오른쪽으로)
```

**왜 이게 중요한가?**

1. **범위 검색 최적화**
   ```sql
   -- ✅ 효율적: 시작점을 찾고 순차적으로 읽기만 하면 됨
   WHERE created_at >= '2024-01-01' AND created_at <= '2024-01-31'

   -- ❌ 비효율적: 전체를 뒤져야 함
   WHERE created_at != '2024-01-01'
   ```

2. **정렬 쿼리 최적화**
   ```sql
   -- ✅ 인덱스 순서 그대로 읽기 (추가 정렬 불필요)
   SELECT * FROM tb_user ORDER BY created_at ASC

   -- ❌ 역순 읽기는 가능하지만 상대적으로 느림
   SELECT * FROM tb_user ORDER BY created_at DESC
   ```

**역인덱스(Descending Index) - MySQL 8.0 이상**

```sql
-- 내림차순 인덱스 생성 (역순으로 저장)
CREATE INDEX idx_created_desc ON tb_user(created_at DESC);

-- 이제 내림차순 정렬도 최적화됨
SELECT * FROM tb_user ORDER BY created_at DESC;
-- Extra: Using index (Filesort 없음)
```

**활용 팁**
- 최신 데이터를 자주 조회하는 경우: DESC 인덱스 고려
- 복합 인덱스에서 컬럼별로 ASC/DESC 지정 가능
  ```sql
  CREATE INDEX idx_user_created
  ON tb_user(name ASC, created_at DESC);
  -- name 오름차순 → 같은 name 내에서 created_at 내림차순
  ```

### 2-9. 클러스터링 인덱스 vs 세컨더리 인덱스

MySQL InnoDB는 두 가지 종류의 인덱스를 사용합니다.

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
SELECT * FROM tb_payment WHERE id = 100;
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
CREATE INDEX idx_company_name ON tb_payment(company_name);

SELECT * FROM tb_payment WHERE company_name LIKE '알서포트%';
-- 1단계: 세컨더리 인덱스(idx_company_name)에서 '알서포트'로 시작하는 PK 목록 추출
--        예: [101, 523, 1247, ..., 98234]
-- 2단계: 추출한 PK로 클러스터링 인덱스에서 실제 데이터 조회 (Random I/O 발생) ⚠️
```

**3. 두 인덱스를 함께 사용할 때의 동작 방식**

```sql
SELECT *
FROM tb_payment
WHERE company_name = '알서포트';
```

**실행 과정 (세컨더리 인덱스 → 클러스터링 인덱스)**

```
Step 1: 세컨더리 인덱스(idx_company_name) 탐색
┌─────────────────────────────────┐
│   idx_company_name (B+Tree)    │
│                                 │
│  Root/Branch: 탐색 경로         │
│         ↓                       │
│  Leaf Nodes:                    │
│  ('알서포트', PK=101)            │
│  ('알서포트', PK=523)            │  ← PK 목록 추출
│  ('알서포트', PK=1247)           │
└─────────────────────────────────┘
         ↓ PK 리스트: [101, 523, 1247]

Step 2: 클러스터링 인덱스(PK)로 실제 데이터 조회
┌─────────────────────────────────┐
│   Clustered Index (B+Tree)     │
│                                 │
│  Root/Branch: PK로 탐색         │
│         ↓                       │
│  Leaf Nodes (실제 데이터):       │
│  PK=101 → (101, '알서포트', ...) │ ← Random I/O
│  PK=523 → (523, '알서포트', ...) │ ← Random I/O
│  PK=1247 → (1247, '알서포트', ...)│ ← Random I/O
└─────────────────────────────────┘
```

**핵심 포인트**
- 세컨더리 인덱스의 리프 노드는 **PK만 저장** (실제 데이터 없음)
- 세컨더리 인덱스 조회 후 **반드시 클러스터링 인덱스를 다시 조회**해야 함
- 이 과정에서 **Random I/O 발생** → 성능 저하의 주요 원인
- Random I/O가 많을수록 성능 저하 → **커버링 인덱스가 중요한 이유**

**발표 멘트**

"MySQL InnoDB는 B+Tree라는 자료구조로 인덱스를 구성합니다.

B+Tree는 세 가지 노드로 구성됩니다. 루트와 브랜치 노드는 키 값만 저장하여 탐색 경로를 제공하고, 리프 노드는 실제 데이터를 저장합니다.

리프 노드는 Linked List로 연결되어 있어 범위 검색에 매우 효율적입니다.

**여기서 정말 중요한 특성이 있습니다.**

인덱스는 기본적으로 왼쪽부터 오른쪽으로 오름차순으로 정렬되어 저장됩니다.

이 특성이 왜 중요할까요?

첫째, 범위 검색이 매우 효율적입니다. '1월 1일부터 1월 31일까지'를 검색하면, 시작점을 찾고 순차적으로 읽기만 하면 됩니다.

둘째, ORDER BY가 인덱스 순서와 일치하면 추가 정렬 작업이 필요 없습니다. 이미 정렬되어 있으니까요.

참고로 MySQL 8.0부터는 역인덱스, 즉 DESC 인덱스도 지원합니다. 최신 데이터를 자주 조회하는 경우 내림차순 인덱스를 만들면 성능이 더 좋아질 수 있습니다.

**마지막으로 매우 중요한 개념, 클러스터링 인덱스와 세컨더리 인덱스의 차이입니다.**

MySQL InnoDB는 두 종류의 인덱스를 사용합니다.

첫째, 클러스터링 인덱스입니다. 이것은 PK 기반 인덱스로, 테이블당 1개만 존재합니다. 리프 노드에 실제 테이블의 모든 데이터가 저장되어 있어서, PK로 조회할 때는 매우 빠릅니다. 인덱스 조회가 곧 데이터 조회니까요.

둘째, 세컨더리 인덱스입니다. 우리가 만든 company_name 인덱스가 바로 세컨더리 인덱스입니다. 이 인덱스의 리프 노드에는 실제 데이터가 없고 PK만 저장되어 있습니다.

따라서 세컨더리 인덱스로 조회하면 2단계를 거쳐야 합니다.

1단계: 세컨더리 인덱스에서 조건에 맞는 PK 목록을 추출합니다.
2단계: 추출한 PK로 클러스터링 인덱스를 다시 조회해서 실제 데이터를 가져옵니다.

이 2단계 과정에서 Random I/O가 발생합니다. Random I/O가 많을수록 성능이 떨어지기 때문에, 인덱스 설계 시 이를 최소화하는 것이 핵심입니다. 그래서 나중에 설명할 커버링 인덱스가 중요한 겁니다.

이제 단일 인덱스로는 해결할 수 없는 더 복잡한 케이스를 살펴보겠습니다."

## 3. 인덱스 설계 (10분)

### 3-1. 복합 조건 검색과 단일 인덱스의 한계

**시나리오**: 사용자 이름 + 상태로 검색

```sql
-- 사용자 이름과 상태로 검색
SELECT * FROM tb_user
WHERE name = 'gmoon'
  AND status = 'ACTIVE';
```

### 3-2. 복합 인덱스 설계 원칙

**현재 인덱스 상황**
```sql
-- name 인덱스만 있는 상태
CREATE INDEX idx_name ON tb_user(name);
```

**EXPLAIN 결과**
```sql
EXPLAIN SELECT * FROM tb_user WHERE name = 'gmoon' AND status = 'ACTIVE';
```

**결과 분석**
- `key: idx_name` → name 인덱스는 사용
- name으로 필터링한 후 status는 테이블에서 재필터링
- status 조건은 인덱스를 활용하지 못함

**문제점**
- name으로 1000건을 찾았다면, 1000건 모두 테이블에서 읽어서 status를 확인
- 불필요한 Random I/O 발생

**복합 인덱스 생성**

**복합 인덱스 생성**
```sql
-- 여러 컬럼을 조합한 인덱스
CREATE INDEX idx_name_status ON tb_user(name, status);
```

**EXPLAIN 재확인**
```sql
EXPLAIN SELECT * FROM tb_user WHERE name = 'gmoon' AND status = 'ACTIVE';
```

**개선 결과**
- 두 조건 모두 인덱스에서 필터링
- 최종 결과에 해당하는 Row만 테이블 접근
- Random I/O 대폭 감소

**복합 인덱스 핵심 원칙**

**복합 인덱스 (Composite Index)**
```sql
CREATE INDEX idx_user_status_date ON tb_user(name, status, created_at);
```
- 여러 컬럼을 조합한 인덱스
- **선행 컬럼 순서가 매우 중요**

**1. 선행 컬럼부터 순차적으로 사용 가능**

전화번호부를 생각해보세요
- 성 → 이름 순으로 정렬
- 성을 알면 빠르게 찾을 수 있지만
- 이름만 알면 찾을 수 없음

```sql
-- 인덱스: (name, status, created_at)

-- ✅ name만 사용
WHERE name = ?

-- ✅ name + status 사용
WHERE name = ? AND status = ?

-- ✅ name + status + created_at 모두 사용
WHERE name = ? AND status = ? AND created_at > ?

-- ❌ 선행 컬럼(name) 없음 - 인덱스 사용 불가
WHERE status = ?

-- ❌ 중간 건너뜀 - status는 인덱스 사용 불가
WHERE name = ? AND created_at > ?
```

**2. 범위 조건(>, <, BETWEEN) 이후 컬럼은 인덱스 사용 불가**

```sql
-- 인덱스: (name, status, created_at)

-- 쿼리
WHERE name = ? AND created_at > ? AND status = ?

-- 실제 인덱스 활용
- name: ✅ 사용 (동등 조건)
- created_at: ✅ 범위 검색까지 사용
- status: ❌ 사용 불가 (범위 조건 이후)
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

범위 조건을 사용하면 그 이후 정렬은 깨진다
```sql
WHERE name = 'gmoon' AND created_at > '2024-01-01'

-- name = 'gmoon'까지는 정렬된 상태
-- 하지만 created_at > '2024-01-01'로 범위를 선택하면
-- 그 안에서 status는 정렬되지 않은 상태
```

### 3-3. 선택도(Selectivity) - 좋은 인덱스의 조건

**선택도 = 전체 데이터 중 특정 값으로 얼마나 필터링되는가**

**높은 선택도 (좋은 인덱스)**
- 예: `email`, `주문번호`, `user_id`
- 값이 고유하거나 다양함 → 필터링 효과 높음
- 인덱스 효율성 ⬆️

**낮은 선택도 (나쁜 인덱스)**
- 예: `gender` (남/여), `status` (활성/비활성)
- 값이 몇 개로 한정 → 필터링 효과 낮음
- 인덱스 효율성 ⬇️

**선택도 계산 방법 (분포도)**

```sql
-- 컬럼의 선택도(분포도) 계산
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

**주의: 데이터 분포 불균형**

분포도가 괜찮아 보여도 데이터 분포가 불균형하면 인덱스를 만들지 않는 게 나을 수 있다.

```
예시:
- 전체 row 수 = 1,000만
- 특정 컬럼 고유 값 수 = 10만
- 분포도 = 1% (10만 / 1,000만 * 100)

→ 분포도가 낮아 인덱스 추가 시 오히려 오버헤드 발생 가능
```

**복합 인덱스 설계 팁**
- **원칙**: 카디널리티(선택도) 높은 컬럼을 앞에 배치
- 단, **실제 쿼리 패턴을 최우선 고려**
- 갱신이 자주 되지 않는 컬럼 선택
- 조인절에 연결되는 컬럼 우선
- 정렬에 사용되는 컬럼 고려

**⚠️ 카디널리티 예외 전략**

일반적으로 카디널리티가 높은 컬럼을 앞에 배치하지만, **IN 절을 Boolean 컬럼으로 변환하여 중간에 배치**하면 성능을 크게 개선할 수 있다.

```sql
-- ❌ IN 절 사용: Binary Search O(log n)
INDEX(user_id, created_at)
WHERE user_id = ? AND user_type IN ('FREE', 'TRIAL', 'BASIC') AND created_at > ?

-- ✅ Boolean 변환: Equality O(1)
INDEX(user_id, is_free_license_user, created_at)
WHERE user_id = ? AND is_free_license_user = true AND created_at > ?
-- 모든 컬럼이 인덱스 활용 + IN 절보다 빠름!
```

**핵심 포인트**
- IN 절 (Binary Search) → Boolean Equality (더 빠름)
- 복합 인덱스에서 Boolean의 낮은 카디널리티는 문제 안 됨
- 데이터 분포가 **5-10% vs 90-95%로 편향**된 경우 특히 효과적
- 자세한 설명: Chapter 7-1 포인트 11 참고

**발표 멘트**

"단일 인덱스로는 한계가 있습니다. 여러 조건으로 검색할 때는 복합 인덱스가 필요합니다.

복합 인덱스의 핵심은 선행 컬럼 순서입니다. 전화번호부처럼 왼쪽 컬럼부터 순차적으로 정렬되어 있기 때문에, 중간을 건너뛰면 인덱스를 사용할 수 없습니다.

특히 범위 조건이 등장하면 그 이후 컬럼은 인덱스를 사용할 수 없다는 점이 중요합니다.

또한 선택도가 높은 컬럼, 즉 고유한 값이 많은 컬럼을 앞에 배치해야 필터링 효과가 좋습니다. 하지만 실제 쿼리 패턴이 더 중요하므로, 통계보다는 실제 사용 빈도를 우선 고려해야 합니다.

이제 더 고급 최적화 기법을 살펴보겠습니다."

### 3-4. 커버링 인덱스 (Covering Index) - 최고의 최적화

**커버링 인덱스 = 인덱스만으로 쿼리를 완전히 처리 가능한 상태**

**일반 인덱스의 동작**
```sql
-- 인덱스
CREATE INDEX idx_name ON tb_user(name);

-- 쿼리
SELECT name, amount, created_at
FROM tb_user
WHERE name = 100;

-- 동작 과정
1. 인덱스에서 name = 100인 PK 찾기
2. PK로 테이블 접근하여 amount, created_at 조회 (Random I/O 발생)
```

**커버링 인덱스의 동작**
```sql
-- 커버링 인덱스
CREATE INDEX idx_covering ON tb_user(name, amount, created_at);

-- 같은 쿼리
SELECT name, amount, created_at
FROM tb_user
WHERE name = 100;

-- 동작 과정
1. 인덱스에서 name = 100 찾기
2. 인덱스에 amount, created_at이 모두 있음
3. 테이블 접근 불필요! (Random I/O 제거)
```

**장점**
- 테이블 데이터 페이지 접근 불필요 → Random I/O 제거
- 가장 빠른 쿼리 성능

**EXPLAIN에서 확인**
- `Extra: Using index` → 커버링 인덱스 사용 중

**실전 활용**
```sql
-- 페이지네이션 최적화
-- 커버링 인덱스로 PK만 빠르게 조회 후, 필요한 데이터만 별도 조회
SELECT *
FROM tb_user
WHERE id IN (
    SELECT id  -- 커버링 인덱스 활용
    FROM tb_user
    WHERE status = 'ACTIVE'
    ORDER BY created_at DESC
    LIMIT 20
);
```

### 3-5. 정렬(ORDER BY)과 인덱스

**인덱스를 활용한 정렬**
- 인덱스는 이미 정렬된 상태로 저장됨
- ORDER BY 컬럼이 인덱스 순서와 일치하면 정렬 작업 생략 가능

```sql
CREATE INDEX idx_created ON tb_user(created_at);
SELECT * FROM tb_user ORDER BY created_at;
-- 인덱스 순서 그대로 읽으면 되므로 추가 정렬 불필요
```

**Filesort 발생 조건**
- ORDER BY 컬럼이 인덱스에 없는 경우
- WHERE 조건과 ORDER BY 컬럼의 인덱스 순서가 다른 경우
- Filesort = 메모리 또는 디스크에서 별도 정렬 작업 수행 → 느림

**복합 인덱스와 ORDER BY**
```sql
-- 인덱스: (status, created_at)
CREATE INDEX idx_status_date ON tb_user(status, created_at);

-- ✅ 인덱스 활용 (Filesort 없음)
WHERE status = 'ACTIVE' ORDER BY created_at

-- ❌ Filesort 발생
WHERE status = 'ACTIVE' ORDER BY name

-- ❌ Filesort 발생 (인덱스 순서와 다름)
ORDER BY created_at, status
```

**발표 멘트**

"커버링 인덱스는 쿼리가 필요로 하는 모든 데이터를 인덱스에서 바로 가져올 수 있는 상태를 말합니다.

테이블 데이터 페이지에 접근할 필요가 없어 Random I/O를 완전히 제거할 수 있습니다. 가장 빠른 쿼리 성능을 보장하는 방법이죠.

EXPLAIN 결과에서 Extra에 'Using index'가 표시되면 커버링 인덱스를 사용하고 있다는 의미입니다.

정렬 최적화도 중요합니다. 인덱스는 이미 정렬된 상태로 저장되기 때문에, ORDER BY가 인덱스 순서와 일치하면 별도 정렬 작업이 필요 없습니다.

그렇지 않으면 Filesort라는 추가 정렬 작업이 발생하여 성능이 떨어집니다.

그렇다면 MySQL 옵티마이저는 이런 인덱스들을 어떻게 선택할까요?"

## 4. 옵티마이저 이해 (5분)

### 4-1. 비용 기반 옵티마이저 (CBO) - 인덱스를 선택하는 주체

**핵심 개념**

쿼리 실행 계획, 즉 어떤 인덱스를 사용할지는 **옵티마이저가 선정**한다.

MySQL 8 버전은 **"비용 기반 옵티마이저"(Cost-Based Optimizer, CBO)** 기반으로 동작한다.

**CBO의 동작 방식**

```
사용자 쿼리 입력
    ↓
옵티마이저가 후보 실행 계획 도출
    ↓
각 실행 계획의 예상 비용 계산 (통계 정보 기반)
    ↓
가장 낮은 비용의 실행 계획 선택
    ↓
쿼리 실행
```

**CBO가 사용하는 통계 정보**
- **데이터량**: 테이블의 전체 Row 수
- **컬럼 값의 수**: 특정 컬럼의 유니크한 값 개수 (Cardinality)
- **컬럼 값 분포**: 데이터 분포도 (MySQL 8.0 히스토그램)
- **인덱스 높이**: B+Tree의 깊이
- **클러스터링 팩터**: 데이터 저장 순서와 인덱스 순서의 일치 정도

**비용 평가 기준**
1. 조건 필터링 효과 (선택도)
2. 정렬 대응 가능 여부
3. JOIN 조건 지원
4. Range vs Const 검색
5. Random I/O vs Sequential I/O 예측

**CBO vs RBO 비교**

| 구분 | CBO (Cost-Based) | RBO (Rule-Based) |
|------|------------------|------------------|
| **방식** | 통계 정보 기반 비용 계산 | 미리 정의된 규칙 우선순위 |
| **동작** | 동적으로 최적 경로 선택 | 고정된 규칙으로 경로 선택 |
| **MySQL** | MySQL 8+ (현재) | 과거 방식 (더 이상 사용 안 함) |
| **장점** | 데이터 특성 반영, 대용량 처리 적합 | 단순하고 예측 가능 |
| **단점** | 통계 정보가 부정확하면 잘못된 선택 | 데이터 특성 무시, 비효율적 |

**RBO의 쿼리 실행 우선순위 (참고)**
```
1. FROM + JOIN
2. WHERE
3. GROUP BY
4. HAVING
5. SELECT
6. ORDER BY
7. LIMIT
```

**Index Dive - CBO의 통계 수집 방식**

CBO는 실행 계획을 세울 때, 실제 테이블 데이터를 **샘플링(Index Dive)** 하여 통계를 확인한다.

- **Index Dive**: 인덱스를 실제로 탐색하여 데이터 분포 확인
- **주의**: 샘플링 과정에서 CPU 사용량 급증 가능
- **대안**: 샘플링 비용이 크면 **인덱스 통계 정보(Index Statistics)** 사용

```sql
-- MySQL 설정으로 Index Dive 동작 제어
-- eq_range_index_dive_limit: IN 절 개수 임계값
-- 기본값을 초과하면 샘플링 대신 통계 정보 사용
SHOW VARIABLES LIKE 'eq_range_index_dive_limit';
```

→ 옵티마이저 튜닝을 통해 DB 성능 개선 가능

### 4-2. EXPLAIN 실행 계획 읽기

**EXPLAIN = MySQL 옵티마이저가 선택한 실행 계획을 보여주는 도구**

```sql
EXPLAIN SELECT * FROM tb_user WHERE name = 100;
```

**주요 컬럼 해석**

**1. type - 접근 방식 (성능 순서)**
- `const`: PK나 유니크 키로 단일 row 조회 → 가장 빠름
- `ref`: 인덱스로 여러 row 조회 → 좋음
- `range`: 인덱스 범위 스캔 (>, <, BETWEEN) → 괜찮음
- `index`: 인덱스 전체 스캔 → 느림
- `ALL`: 테이블 전체 스캔 → 가장 느림 ⚠️

**2. key - 실제 사용된 인덱스**
- `NULL`: 인덱스 미사용 → 문제 발생 ⚠️
- 인덱스 이름 표시: 해당 인덱스 사용 중

**3. rows - 옵티마이저가 예상하는 검색 row 수**
- 적을수록 좋음
- 실제 테이블 크기 대비 비율 확인

**4. Extra - 추가 정보**
- `Using index`: 커버링 인덱스 사용 → 최고의 성능 ✅
- `Using where`: WHERE 조건 필터링
- `Using filesort`: 별도 정렬 작업 발생 → 개선 필요 ⚠️
- `Using temporary`: 임시 테이블 사용 → 개선 필요 ⚠️

**실전 예시**

```sql
-- ❌ 나쁜 예시
EXPLAIN SELECT * FROM tb_user WHERE DATE(created_at) = '2024-01-01';
-- 결과: type: ALL, key: NULL, rows: 1000000
-- 문제: 함수 사용으로 인덱스 무효화, Full Scan 발생

-- ✅ 개선 예시
EXPLAIN SELECT * FROM tb_user
WHERE created_at >= '2024-01-01' AND created_at < '2024-01-02';
-- 결과: type: range, key: idx_created_at, rows: 1500
-- 개선: 인덱스 활용, 대폭 성능 향상 (1000배 이상)
```

### 4-3. 통계 정보 관리 - ANALYZE TABLE

**통계 정보가 부정확하면?**

CBO는 통계 정보를 기반으로 비용을 계산하므로, **통계가 부정확하면 잘못된 인덱스를 선택할 수 있다.**

**ANALYZE TABLE로 통계 정보 갱신**

```sql
-- 특정 테이블의 통계 정보 갱신
ANALYZE TABLE tb_user;

-- 여러 테이블 동시 갱신
ANALYZE TABLE tb_user, users, products;
```

**언제 실행해야 하나?**
- 대량 데이터 INSERT/DELETE 후
- 인덱스 추가/삭제 후
- 쿼리 성능이 갑자기 느려졌을 때
- 정기적인 유지보수 (예: 주 1회)

**주의사항**
- ⚠️ **Read Lock 발생**: 실행 중 테이블에 Read Lock이 걸림
- ⚠️ **서비스 시간 피하기**: 트래픽이 적은 새벽 시간대 권장
- ⚠️ **큰 테이블 주의**: 데이터가 많으면 시간이 오래 걸림

**MySQL 8.0 히스토그램**

```sql
-- 히스토그램 생성 (컬럼 값 분포 수집)
ANALYZE TABLE tb_user UPDATE HISTOGRAM ON created_at, status;

-- 히스토그램 삭제
ANALYZE TABLE tb_user DROP HISTOGRAM ON created_at;
```

- 더 정확한 데이터 분포 정보 제공
- CBO의 비용 예측 정확도 향상

**발표 멘트**

"인덱스를 설계하는 것도 중요하지만, 실제로 어떤 인덱스를 사용할지 결정하는 것은 옵티마이저입니다.

MySQL 8은 비용 기반 옵티마이저, CBO를 사용합니다. CBO는 통계 정보를 기반으로 각 실행 계획의 비용을 계산하여 가장 효율적인 경로를 선택합니다.

CBO가 사용하는 통계 정보에는 데이터량, 컬럼 값의 수, 데이터 분포, 인덱스 높이 등이 있습니다. 이 정보가 정확해야 옵티마이저가 올바른 선택을 할 수 있습니다.

옵티마이저가 선택한 실행 계획을 확인하려면 EXPLAIN을 사용합니다.

중요한 컬럼 네 가지를 살펴보겠습니다.

type은 접근 방식입니다. const가 가장 빠르고 ALL이 가장 느립니다. type이 ALL이면 테이블 전체를 스캔한다는 의미이므로 반드시 개선이 필요합니다.

key는 실제 사용된 인덱스를 보여줍니다. NULL이면 인덱스를 사용하지 않는다는 의미로 문제가 있습니다.

rows는 MySQL이 예상하는 검색 row 수입니다. 이 숫자가 크면 성능이 떨어질 가능성이 높습니다.

Extra는 추가 정보를 제공합니다. 'Using index'는 커버링 인덱스를 사용한다는 의미로 최고의 성능입니다. 반면 'Using filesort'나 'Using temporary'가 나오면 개선이 필요합니다.

특히 주의할 점은, WHERE 조건에 함수를 사용하면 인덱스가 무효화된다는 것입니다. 예시를 보면 DATE 함수를 사용하면 Full Scan이 발생하지만, 범위 조건으로 바꾸면 인덱스를 타서 1000배 이상 성능이 향상됩니다.

마지막으로 통계 정보 관리입니다. CBO는 통계 정보를 기반으로 동작하므로, 통계가 부정확하면 잘못된 인덱스를 선택할 수 있습니다.

ANALYZE TABLE 명령으로 통계 정보를 갱신할 수 있습니다. 대량 데이터 변경 후나 인덱스 추가/삭제 후에는 반드시 실행해야 합니다. 단, Read Lock이 발생하므로 트래픽이 적은 시간대에 실행하는 것이 좋습니다.

이제 마지막으로 실전 튜닝 사례를 살펴보겠습니다."

## 5. 실전 튜닝 사례 (3분)

### 5-1. 실제 개선 사례: 관리자 페이지 48초 → 124ms 개선

**발표 멘트**

"Chapter 1에서 말씀드렸던 파트너 어드민 사이트의 504 타임아웃 이슈, 실제로 어떻게 해결했는지 보여드리겠습니다.

이 쿼리는 운영 중 발생했던 슬로우 쿼리를 개선한 실제 사례입니다. 48초 걸리던 쿼리를 124ms로, 약 387배 성능을 개선했습니다."

**개선된 쿼리**

```sql
-- 관리자 페이지 조회: 48s → 124ms (387배 개선)
EXPLAIN
SELECT
    -- 1. 불필요한 프로젝션 정리
    team0_.id,
    team0_.type,
    team0_.status,

    -- 2. 스칼라 서브 쿼리 적용 (JOIN 제거로 성능 개선)
    (SELECT user2_.name
     FROM tb_user user2_
     WHERE user2_.company_id = company0_.id
       AND role = 'ADMIN') AS col_12_0_
FROM tb_team team0_
         INNER JOIN tb_company company0_ ON team0_.company_id = company0_.id
WHERE
    -- 3. Loose Index Scan 설정 (강제로 인덱스 타게 하기)
    (team0_.created_dt BETWEEN '1900-01-01' AND '2099-01-01')

    -- 4. 커버링 인덱스 적용
    AND (team0_.company_id IN (SELECT company4_.id
                               FROM tb_company company4_
                               -- 5. LIKE 문자열 검색 최적화
                               WHERE (company4_.name LIKE 'gmoon%')))
ORDER BY team0_.created_dt DESC
LIMIT 15 -- 6. no-offset 페이지네이션
```

**핵심 개선 포인트 11가지**

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
   - [자세한 내용](https://github.com/gmoon92/toy/blob/master/spring-jpa/spring-jpa-pagination/doc/cursor-pagination.md)

**8. Loose Index Scan 고려 (차선책)**
   - 옵티마이저가 의도한 인덱스를 타지 않을 때
   - 강제로 인덱스 컬럼 범위를 지정하여 Index Scan 유도
   - ⚠️ 올바른 튜닝은 아니지만, 차선책으로 고려 가능

   **Loose Index Scan 조건:**
   - 커버링 인덱스 적용
   - `SELECT DISTINCT`, `GROUP BY`, 단일 튜플 SELECT
   - 집계 함수에 `DISTINCT` 포함 (MIN/MAX 제외)
   - `COUNT(*)`는 사용 불가
   - 부분 키 카디널리티가 전체의 1/100 이하

**9. 인덱스 최적화 명령**
   ```sql
   -- 인덱스 재구성 (단편화 제거)
   OPTIMIZE TABLE table_name;

   -- 테이블 통계 업데이트
   ANALYZE TABLE table_name;
   ```

**10. WHERE IN 절 최적화**
   - IN 절의 값 개수에 따라 실행 계획이 달라질 수 있음
   - 옵티마이저는 IN 절을 **"equality range comparison"**으로 최적화 처리

   ```sql
   -- IN 절은 의미상 OR와 동일하지만
   WHERE id IN ('id1', 'id2', 'id3', 'id4', 'id5')

   -- 단순 OR 변환이 아닌 equality range로 최적화됨
   -- WHERE id = 'id1' OR id = 'id2' ... (X)
   -- equality range comparison (O)
   ```

   **MySQL 옵티마이저 동작 방식** ([MySQL 8.0 공식 문서](https://dev.mysql.com/doc/refman/8.0/en/range-optimization.html))
   - IN 절 값이 **적을 때**: Index Dive로 정확한 통계 수집
   - IN 절 값이 **많을 때**: Index Statistics로 빠른 추정
   - 전환점: `eq_range_index_dive_limit` 설정값 (기본 200)

   ```sql
   -- 설정 확인 (Chapter 6-1 Index Dive 참고)
   SHOW VARIABLES LIKE 'eq_range_index_dive_limit';
   ```

   - IN 절 개수가 많으면 (200개 이상) 성능 저하 가능성
   - 대안: 서브쿼리, 임시 테이블, 또는 애플리케이션 레벨 분할 처리

**11. 카디널리티 예외 전략: IN 절을 Boolean으로 변환**

   일반적으로 카디널리티가 높은 컬럼(고유 값이 많은 컬럼)을 복합 인덱스 앞에 배치하는 것이 원칙이지만, **IN 절을 Boolean 컬럼으로 변환하여 복합 인덱스 중간에 배치**하면 성능을 크게 개선할 수 있음

   **문제 상황: IN 절 사용**
   ```sql
   -- 사용자 유형이 20개 존재하는 경우
   -- 기존 인덱스: (user_id, created_at)
   CREATE INDEX idx_user_date ON users(user_id, created_at);

   -- 쿼리: 무료 라이센스 사용자만 조회 (전체의 5-10%)
   SELECT * FROM users
   WHERE user_id = 100
     AND user_type IN ('FREE', 'TRIAL', 'BASIC')  -- Binary Search O(log 3)
     AND created_at > '2024-01-01';

   -- 문제: IN 절 비용 + 인덱스 활용 제한
   ```

   **해결 전략: Boolean Discriminator Column 추가**
   ```sql
   -- 1. Boolean 컬럼 추가 (애플리케이션에서 데이터 저장 시 설정)
   ALTER TABLE users ADD COLUMN is_free_license_user BOOLEAN DEFAULT false;

   -- 또는 Computed Column (MySQL 5.7+)
   ALTER TABLE users ADD COLUMN is_free_license_user BOOLEAN
   GENERATED ALWAYS AS (user_type IN ('FREE', 'TRIAL', 'BASIC')) STORED;

   -- 2. 복합 인덱스 재구성 (Boolean 컬럼을 중간에 배치)
   CREATE INDEX idx_user_license_date ON users(user_id, is_free_license_user, created_at);

   -- 3. 개선된 쿼리
   SELECT * FROM users
   WHERE user_id = 100
     AND is_free_license_user = true  -- Equality O(1) - 빠름!
     AND created_at > '2024-01-01';
   ```

   **성능 비교**
   ```sql
   -- ❌ IN 절 방식
   WHERE user_type IN ('FREE', 'TRIAL', 'BASIC')
   -- MySQL은 IN 리스트를 정렬 후 Binary Search: O(log n)
   -- 값이 많아질수록 (200개 이상) 성능 저하

   -- ✅ Boolean 방식
   WHERE is_free_license_user = true
   -- Equality 비교: O(1) - 항상 빠름
   -- 복합 인덱스에서 낮은 카디널리티 문제 없음
   ```

   **왜 효과적인가?**
   - ✅ **IN 절보다 빠름**: Binary Search O(log n) → Equality O(1)
   - ✅ **복합 인덱스 전체 활용**: (user_id, is_free_license_user, created_at) 모두 사용
   - ✅ **Range Scan 가능**: created_at 범위 조건도 인덱스 활용
   - ✅ **데이터 분포 편향 활용**: is_free_license_user = true가 5-10%일 때 특히 효과적
   - ✅ **스케일 가능**: user_type이 3개든 20개든 성능 동일

   **복합 인덱스에서 Boolean 컬럼의 위치**
   ```sql
   -- ✅ 올바른 설계: 고카디널리티 → Boolean → 범위 조건
   INDEX(user_id, is_free_license_user, created_at)
   --    ↑ 높은 카디널리티  ↑ Boolean (중간)  ↑ Range

   -- ❌ 잘못된 설계: Boolean을 맨 앞에 배치
   INDEX(is_free_license_user, user_id, created_at)
   -- Boolean의 낮은 카디널리티로 인덱스 효율 저하
   ```

   **MySQL 공식 문서 근거**
   - **복합 인덱스에서 Boolean의 낮은 카디널리티는 문제되지 않음**
   - 첫 컬럼(user_id)으로 데이터가 충분히 필터링되면, Boolean 컬럼이 추가 필터 역할
   - 참고: [MySQL Composite Index Best Practices](https://dev.mysql.com/doc/refman/8.0/en/multiple-column-indexes.html)

   **실제 벤치마크 비교**
   ```sql
   -- 테스트 환경: 1,000,000 rows, user_type 20개, FREE 사용자 5%

   -- ❌ IN 절 방식: 평균 120ms
   WHERE user_id = ? AND user_type IN ('FREE', 'TRIAL', 'BASIC')

   -- ✅ Boolean 방식: 평균 8ms (15배 빠름)
   WHERE user_id = ? AND is_free_license_user = true
   ```

   **vs 임시 테이블 방식 비교**
   ```sql
   -- ❌ 임시 테이블: 쿼리 실행 시마다 오버헤드
   SELECT * FROM users u
   WHERE user_id = 100
     AND user_type IN (SELECT type FROM temp_free_types)  -- 조인 비용
     AND created_at > '2024-01-01';

   -- ✅ Boolean 컬럼: 저장 시 한 번만 설정, 쿼리 실행 시 비용 없음
   WHERE user_id = 100
     AND is_free_license_user = true  -- 즉시 필터링
     AND created_at > '2024-01-01';
   ```

   **주의사항**
   - 데이터 분포가 **편향된 경우**에만 유효 (예: 5-10% vs 90-95%)
   - Boolean 값이 균등하면 (50% vs 50%) 효과 제한적
   - 스토리지 공간 약간 증가 (Boolean 컬럼 1바이트)
   - 애플리케이션 코드에서 Boolean 값을 일관되게 관리 필요

**발표 멘트**

"이 사례에서 가장 큰 효과를 본 것은 3가지입니다.

첫째, 불필요한 JOIN을 스칼라 서브 쿼리로 변경했습니다. JOIN으로 인한 중복 데이터 처리 비용이 제거되었습니다.

둘째, 커버링 인덱스를 적용하여 테이블 데이터 페이지 접근을 완전히 제거했습니다.

셋째, no-offset 페이지네이션으로 불필요한 데이터 스캔을 제거했습니다.

결과적으로 48초 걸리던 쿼리가 124ms로 단축되었고, 504 타임아웃 이슈가 완전히 해결되었습니다."

### 5-2. 현업에서 당장 써먹을 핵심 체크리스트

**발표 멘트**

"지금까지 배운 내용을 실전에서 바로 적용할 수 있도록 핵심 체크리스트를 정리하겠습니다."

**1. 인덱스 무효화 조건 피하기**
```sql
-- ❌ 나쁜 예시
WHERE DATE(created_at) = '2024-01-01'  -- 함수 사용
WHERE name + 1 = 100                -- 연산 사용
WHERE name LIKE '%검색어%'             -- 중간 와일드카드

-- ✅ 좋은 예시
WHERE created_at >= '2024-01-01' AND created_at < '2024-01-02'
WHERE name = 99
WHERE name LIKE '검색어%'              -- 앞 고정 와일드카드
```

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

**발표 멘트**

"핵심만 정리하겠습니다.

첫째, WHERE 절에 함수나 연산을 쓰지 마세요. 인덱스가 무효화됩니다.

둘째, 복합 인덱스는 동등 조건을 앞에, 범위 조건을 뒤에 배치하세요.

셋째, 커버링 인덱스로 Random I/O를 완전히 제거하세요.

넷째, ORDER BY 컬럼을 인덱스에 포함시켜 Filesort를 방지하세요.

다섯째, 가장 중요한 것은 항상 EXPLAIN으로 검증하는 습관입니다.

이것으로 발표를 마치겠습니다. 질문 있으시면 말씀해 주세요."

## 6. Q&A (1분)

**예상 질문**
- Q: 인덱스를 너무 많이 만들면 문제가 되나요?
  - A: 네, INSERT/UPDATE 성능이 저하됩니다. 인덱스도 관리 비용이 있으므로 실제 사용되는 쿼리 패턴에 맞춰 필요한 인덱스만 생성해야 합니다.

- Q: JPA에서 인덱스는 어떻게 관리하나요?
  - A: Entity 클래스의 `@Table(indexes = {...})` 또는 `@Index` 애노테이션으로 정의할 수 있습니다. 하지만 실전에서는 DDL을 직접 관리하는 것이 더 명확합니다.

- Q: 인덱스 개선 효과는 어느 정도인가요?
  - A: 실제 사례에서 10~100배 성능 개선은 흔하며, 잘 설계하면 1000배 이상도 가능합니다. 특히 대용량 테이블일수록 효과가 극적입니다.

- Q: 우린 MySQL 이 아닌데요?
  - A: 인덱스 자료구조가 B+Tree 라면 기본 동작 메커니즘은 동일합니다. 다만. 다른 부분이 있다면 옵티마이저의 실행 계획에 따른 인덱스 선정 방식이 달라질 순 있습니다.


---

**발표 종료**

감사합니다!
