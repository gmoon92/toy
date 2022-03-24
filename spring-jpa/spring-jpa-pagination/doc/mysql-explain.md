# MySQL Explain 실행 계획

## TOC

- [EXPLAIN Statement](#explain-statement)
- [EXPLAIN Output Columns](#explain-output-columns)
    - [id](#id)
    - [select_type](#select_type)
    - [table](#table)
    - [possible_keys](#possible_keys)
    - [type](#type)
    - [key](#key)
    - [key_len](#key_len)
    - [ref](#ref)
    - [rows](#rows)
    - [filtered](#filtered)
    - [Extra](#extra)

## EXPLAIN Statement

```sql
EXPLAIN
SELECT [col_name]
FROM [table_name]
WHERE [condition]


EXPLAIN FORMAT = JSON
SELECT [col_name]
FROM [table_name]
WHERE [condition]
```

## EXPLAIN Output Columns

| Column                                | JSON Name   | 의미                               |
|---------------------------------------|-------------|----------------------------------|
| [**`id`**](#id)                       | select_id   | SELECT를 구분하는 번호                  | 
| [**`select_type`**](#select_type)     | None        | SELECT에 대한 타입                    | 
| [**`table`**](#table)                 | table_name  | 참조하는 테이블                         |
| [**`possible_keys`**](#possible_keys) | partitions  | 데이터를 조회할 때 DB에서 사용할 수 있는 인덱스 리스트 |
| [**`type`**](#type)                   | access_type | 조인 혹은 조회 타입                      |
| [**`key`**](#key)                     | key         | 실제로 사용할 인덱스                      |
| [**`key_len`**](#key_len)             | key_length  | 실제로 사용할 인덱스의 길이                  |
| [**`ref`**](#ref)                     | ref         | Key 안의 인덱스와 비교하는 컬럼(상수)          |
| [**`rows`**](#rows)                   | rows        | 쿼리 실행 시 조사하는 행 수립                |
| [**`filtered`**](#filtered)           | filtered    | 테이블 조건으로 필터링된 행의 백분율             |
| [**`Extra`**](#extra)                 | None        | 추가 정보                            |

### id

쿼리에 대한 실행 단위를 식별하기 위해, `SELECT`을 구분하는 번호다.

`MySQL`은 `JOIN`을 하나의 실행 단위로 인식한다. 하나의 `SELECT` 문에서 여러 개의 테이블을 조인하면 조인되는 테이블의 개수만큼 실행 계획 `row`가 출력되지만 같은 id가 부여된다.

만약 쿼리안에 `Sub Query`, `UNION`과 같은 서로 다른 실행 단위로 구성되어 있다면, 각 `SELECT`의 `id`는 다른 값으로 부여된다.

```sql
EXPLAIN
SELECT 1
  FROM lt_user_login AS A
 WHERE A.access_device = 'WEB'
 UNION
SELECT 2
  FROM lt_user_login AS B
  JOIN lt_user_login C ON B.id = C.id
 WHERE B.access_device = 'MOBILE'
   AND B.username IN (SELECT D.username
                        FROM lt_user_login AS D
                       WHERE D.username = 'admin')
```

| id|select_type| table|type| possible_keys |key|key_len|ref|rows|Extra|
|---|---|--------|---|---|---|---|---|---|---|
|1|PRIMARY| A             |ALL|`<null>`|`<null>`|`<null>`|`<null>`|60|`Using where`|
|2|UNION| B             |ALL| PRIMARY,idx_username  |`<null>`|`<null>`|`<null>`|60|`Using where`|
|2|UNION| `<subquery3>` |eq_ref| distinct_key  |distinct_key|203|func|1||
|2|UNION|C|eq_ref|PRIMARY|PRIMARY|202|toy_spring_jpa_pagination.B.id|1|Using index |
|3|MATERIALIZED| D             |ref| idx_username  |idx_username|203|const|60| `Using where`; `Using index` |
|`<null>`|UNION RESULT| `<union1,2>`  |ALL|`<null>`|`<null>`|`<null>`|`<null>`|`<null>`||

### select_type

SELECT 유형은 다음과 같다.

| select_type          | 의미                                                                                                                                                                                                          |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SIMPLE               | Simple SELECT 문은 `UNION` 또는 `Sub Query`를 사용하지 않는 경우                                                                                                                                                         |
| PRIMARY              | 가장 바깥의 SELECT 문을 의미한다. <br/> Sub Query를 사용할 경우 Sub Query의 외부에 있는 첫 번째 쿼리<br/> UNION 인 경우엔 UNION의 첫 번째 SELECT 쿼리                                                                                             |
| UNION                | UNION 문에서 Primary 유형을 제외한 나머지 SELECT                                                                                                                                                                        |
| DEPENDENT_UNION      | UNION 과 동일하나, 외부 쿼리에 의존적인 경우 <br/>union으로 결합된 단위 쿼리가 바깥쪽 쿼리에 의존적이어서 외부의 영향을 받고 있는 경우                                                                                                                        |
| UNION_RESULT         | UNION 쿼리에 대한 결과                                                                                                                                                                                             |
| SUBQUERY             | 첫 번째 SELECT 문의 Sub Query                                                                                                                                                                                    |
| DEPENDENT_SUBQUERY   | SUBQUERY 와 동일하나, 외부 쿼리에 의존적인 경우<br/> 이는 Sub Query가 먼저 실행되지 못하고 Sub Query가 외부 쿼리 결과에 의존적이기 때문에 전체 쿼리의 성능을 느리게 만든다. Sub Query가 외부의 쿼리의 값을 전달받고 있는지 검토해서, 가능하다면 외부 쿼리와의 의존도를 제거하는 것이 좋다.                       |
| DERIVED              | `Derived table`<br/>`from 절에 사용된 Sub Query`(inline view)로 부터 발생한 임시 테이블을 의미한다. 임시 테이블은 메모리에 저장될 수도 있고, 디스크에 저장될 수도 있다. 일반적으로 메모리에 저장하는 경우에는 성능에 큰 영향을 미치지 않지만, 데이터의 크기가 커서 임시 테이블을 디스크에 저장할 경우 성능이 떨어지게 된다. |
| DEPENDENT_DERIVED    | `Derived table` 이 다른 테이블과 의존적인 경우                                                                                                                                                                           |
| MATERIALIZED         | MySQL 5.6 버전에 추가된 유형으로 IN 절 내의 Sub Query를 임시 테이블로 만들어 조인을 하는 형태로 최적화 해준다. DERIVED 와 비슷한 개념이다.                                                                                                               |
| UNCACHEABLE_SUBQUERY | Sub Query는 종류에 따라 바깥쿼리 row 수만큼 수행되어야 하는 경우도 있다. <br/>실제로 그렇게 작동한다면 성능에 큰 영향을 끼치게 되므로 때에 따라 쿼리를 캐싱해놓고 캐싱 된 데이터를 갖다 쓰게끔 최적화가 되어있는데 그런 캐싱이 작동할 수 없는 경우에 표현된다. 즉 캐싱 되지 못하는 이유가 수정 가능하다면 캐싱 되게끔 하는 것이 성능에 좋다.    |
| UNCACHEABLE_UNION    | 캐싱하지 못하는 `UNION Sub Query`                                                                                                                                                                                  |

### table

참조되는 테이블을 의미한다.

테이블 이름에 별칭(Alias)이 있다면 별칭 명으로, 별칭이 없다면 테이블 명으로 표기된다.

### possible_keys

옵티마이저가 최적의 실행 계획을 만들기 위해 후보로 선정했던 인덱스들을 의미한다.

### type

옵티마이저가 테이블의 row에 대한 접근 방식을 의미한다.

접근 방식은 대상 테이블로의 접근이 효율적일지 판단하는 데 가장 중요한 항목이다.

아래 접근 방식 가운데도 주의가 필요한 유형은 `ALL`, `index`, `ref_or_null`이다. 특히 `ALL`, `index`는 테이블 또는 특정 인덱스의 전체 `row`에 접근하기 때문에 테이블 크기가
크면 효율이 크게 떨어진다.
`ref_or_null`의 경우엔 `row`에 `NULL`이 포함되어 있다면, 인덱스의 맨 앞에 모아서 저장하지만, 그 건수가 많으면 MySQL 서버의 작업량이 방대해진다. 다시 말해서 ALL 이외의 접근 방식은
모두 인덱스를 사용한다.

접근 방식이 `ALL` 또는 `index`인 경우는 그 쿼리로 사용할 수 있는 적절한 인덱스가 없다는 의미일 수도 있다. 테이블에 대한 접근은 `ALL`이지만 WHERE 절의 조건을 지정하지 않는 쿼리에서 드라이빙
테이블에 접근한다면 전체 행을 스캔할 수밖에 없다.

| type                                                                                | 의미                                                                                                                                                                                                                                                                                                                                                                                              |
|-------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| system                                                                              | 테이블의 row 수가 1행 밖에 없는 특수한 경우(system table) <br/>또 다른 경우는 `const` join type일 경우를 의미한다.                                                                                                                                                                                                                                                                                                            |
| const                                                                               | 테이블의 row 수와 관계 없이 `Primary key`나 `Unique key` 컬럼으로 where 절 조건을 가지고 있으며 반드시 1건만 반환하는 쿼리 처리 방식<br/> 단 범위 검색으로 지정하는 경우 const가 되지 않는다.                                                                                                                                                                                                                                                              |
| eq_ref                                                                              | 일반적인 join 형식을 의미한다.<br/> `eq_ref` 는 system 이나 const 유형을 제외하고 가장 적합한 Join 유형이다. <br/> Join 작동하는 원리는 일단 첫 번째 테이블(첫 번째로 어떤 걸 드라이빙할지는 옵티마이저가 정한다. table1이 앞이라고 꼭 table1을 먼저 탐색하지 않는다.)을 조회한 후 해당 컬럼을 조건절에 대입함으로 이루어진다. 이때 첫 번째 드라이빙 테이블의 결과가 두 번째 테이블의 unique 인덱스가 걸려있는 컬럼일 경우 `eq_ref`가 표현되게 된다. <br/>조금 간단히 말하면 Join시 기본 키를 이용하면 된다. unique 인덱스를 이용해 탐색하는 건 일단 기본적으로 빠르므로 이 역시 튜닝 대상에서 고려할 필요가 없다. |
| ref                                                                                 | `eq_ref`와 달리 인덱스의 종류와 상관없이 동등 조건으로 검색할 때 사용되는 접근 방식이다.<br/>반환되는 row가 반드시 한 건이라는 보장이 없으므로 `eq_ref`보다 빠르지 않다.                                                                                                                                                                                                                                                                                     |
| ref_or_null                                                                         | `ref`와 동일하면서 null 비교까지 추가된 접근 방식                                                                                                                                                                                                                                                                                                                                                                |
| fulltext                                                                            | `Full Text`(전문 검색) 인덱스를 사용하여 row를 읽는 방법<br/>해당 인덱스를 이용하기 위해서는 `MATCH AGAINST` 라는 특정 문법을 활용해야한다. 또한 fulltext 인덱스가 생성되어 있어야하며 해당 인덱스가 없는데 `MATCH AGAINST` 문법을 사용하면 에러가 발생하게된다. 확연히 `FullText` 인덱스보다 빠른 인덱스가 있는게 아니라면 옵티마이저는 일단 `FullText` 인덱스를 사용하려고 하게된다. `ref`보다는 성능이 낮다.                                                                                                                       |
| [index_merge](https://dev.mysql.com/doc/refman/8.0/en/index-merge-optimization.html) | 2개 이상의 인덱스를 사용해 검색 결과를 만들어낸 후에 그 결과를 병합하는 처리 방식(Full Text index 제외)                                                                                                                                                                                                                                                                                                                             |
| unique_subquery                                                                     | IN 서브쿼리 접근에서 `Primary key`또는 `Unique key`를 사용한다. 이 방식은 쓸데 없는 오버헤드를 줄여 상당히 빠르다.                                                                                                                                                                                                                                                                                                                  |
| index_subquery                                                                      | `unique_sunquery`와 거의 비슷하지만 고유한 인덱스를 사용하지 않는 점이 다르다. <br/>예를 들어 IN 절 안에 서브 쿼리가 존재하는 경우, 서브 쿼리가 중복된 값들을 반환한다면 IN 절 내에서 사용되기 위해서 중복 값들을 제거해야한다. 이때 인덱스를 사용해서 중복값을 제거한다면 `index_subquery`가 표현된다. 이 접근 방식도 상당히 빠르다                                                                                                                                                                                  |
| range                                                                               | 인덱스 특정 범위의 `row`에 접근한다.<br/>인덱스를 동등 비교가 아닌 범위 비교시 발생하는 가장 많이 사용되는 방식이다. `<`, `>`, `LIKE` 가 대표적인 연산자이다.<br/>범위를 탐색하기 때문에 다른 타입에 비해 성능이 좋은편은 아니지만, 극도의 최적화를 원하는게 아니라면 괜찮다고 볼 수 있다.                                                                                                                                                                                                                  |
| index                                                                               | 인덱스 스캔, 테이블의 특정 인덱스의 전체 엔트리에 접근한다.<br/>`range`가 필요한 인덱스 범위를 지정해서 탐색하는 방식이라면 `index`는 인덱스를 전부 스캔하는 방식이다. <br/>인덱스도 하나의 테이블로 관리되기 때문에 어찌보면 테이블 풀스캔이라고 볼 수도 있는 방식이다. <br/>하나 안심할 거리라면 인덱스 테이블이 데이터 테이블보다는 크기가 작다는 것이다. 위 존재하는 다른 방식들을 사용할 수는 없지만 데이터 테이블까지 가지않고 인덱스만으로 처리가 가능할 때 표현된다. <br/>즉 `Table full scan`을 피하기 위한 최후의 보루라고 볼 수 있다.                                                           |
| ALL                                                                                 | 전체 행 스캔, 테이블의 데이터 전체에 접근한다. 대부분의 경우 아주 느린 성능을 보인다.<br/>이것이 바로 `Table full scan`이다. MySQL이나 MariaDB 같은 경우 `Table full scan`이 발생했을 때도 최적화를 통해 최대한 빨리 모든 테이블을 스캔할 수 있도록 하기는 하지만 인덱스 찾아서 가는 방식보단 당연히 성능에 이슈가 발생한다.&lt;br/&gt;index, all 방식과 같은 풀스캔 타입은 튜닝시 제거 1순위인 항목들이다.                                                                                                                           |

### key

`possible_keys` 중에서 옵티마이저가 실제로 사용한 인덱스를 의미한다.

### key_len

선택된 인덱스의 길이를 의미한다. `key` 컬럼이 `NULL`이면 `key_len` 컬럼도 `NULL`이 출력된다.

### ref

key 컬럼에 지정된 인덱스가 참조 조건으로 어떤 컬럼이 제공되었는지 나타낸다.

참조 대상으로 상수를 지정했다면 `const`로 표시되고, 다른 테이블의 컬럼 값이면 그 테이블 명과 컬럼 명이 표시된다. `func`은 "Function"의 줄임말로, 참조하는 컬럼의 값이 그대로 사용된 것이
아니라 콜레이션(Collation) 변환이나 값 자체 연산을 거쳐서 참조됐다는 것을 의미한다.

> 콜레이션(Collation)이란 데이터베이스에서 검색이나 정렬과 같은 작업을 할 때 사용하는 비교를 위한 규칙의 집합을 의미한다. `Collation`은 텍스트 데이터를 정렬(ORDER BY) 할 때 사용한다.

### rows

`rows`는 실행 계획의 효율성 판단을 위해 예측했던 row 수를 의미한다.

> `rows`는 각 스토리지 엔진별로 가진 통계 정보를 참조하여 `MySQL`의 옵티마이저가 산출해낸 예상값으로 정확하지 않다.

### filtered

테이블 조건에 따라 필터링된 테이블 행의 **`예상 백분율`** 을 의미한다.

최댓값은 `100`으로 필터링이 실행되지 않았음을 의미한다. 100에서 감소하면 필터링 양이 증가하고 있음을 뜻한다.

예를 들어 `rows`가 `1,000`이고 `filtered` 값이 50.00(50%)이라면, 조건에 의해 반환되는 행의 수는 1000 × 50% = 500 이다.

### Extra

실행 계획에서 쿼리를 어떻게 해석하는지에 관한 추가적인 정보가 출력된다.

따라서 [`Extra`](https://dev.mysql.com/doc/refman/8.0/en/explain-output.html#explain-extra-information) 컬럼은 옵티마이저가 동작하는 데
대해서 우리에게 알려주는 힌트로써 `EXPLAN`을 사용해 옵티마이저의 행동을 파악할 때 아주 중요한 컬럼이다.

| Extra                                 | 의미                                                                                                                                                                                                                                                                                                                                                 |
|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Backward index scan                   | 옵티마이저는 InnoDB 테이블에서 내림차순 인덱스를 사용할 수 있다. `Using index`와 함께 표시된다.<br/> 자세한 내용은 [Descending Indexes](https://dev.mysql.com/doc/refman/8.0/en/descending-indexes.html) 참고하자.                                                                                                                                                                           |
| const row not found                   | type 컬럼이 const인데 해당 테이블에 1건도 존재하지 않으면 표시된다.                                                                                                                                                                                                                                                                                                        |
| Distinct                              | MySQL이 값을 찾은 경우, 같은 조건을 만족하는 또 다른 행이 있는지 다시 검색하지 않는다.                                                                                                                                                                                                                                                                                              |
| Using where                           | 접근 방식을 설명한 것으로, 테이블에서 행을 가져온 후 추가적으로 검색조건을 적용해 행의 범위를 축소한 것을 표시한다.<br/>MySQL은 내부적으로 크게 `MySQL 엔진`과 `스토리지 엔진` 두 개의 레이어로 나눠서 볼 수 있다. <br/>`스토리지 엔진`은 디스크나 메모리 상에서 필요한 `row`를 읽거나 저장하는 역할을 하며, <br/>`MySQL 엔진`은 `스토리지 엔진`으로부터 받은 `row`를 가공 또는 연산하는 작업을 수행한다. <br/>`MySQL 엔진` 레이어에서 별도의 가공을 해서 필터링 작업을 처리한 경우 이 키워드가 나타난다.                                 |
| Using index                           | 테이블에는 접근하지 않고 인덱스에서만 접근해서 쿼티를 해결하는 것을 의미한다. <br/>커버링 인덱스로 처리됨 `index only scan`이라고도 부른다.<br/>데이터 파일을 전혀 읽지 않고 인덱스만 읽어서 쿼리를 모두 처리할 수 있을 때 나타나는 키워드                                                                                                                                                                                                  |
| Using index for group-by              | Using index와 유사하지만 `GROUP BY`가 포함되어 있는 쿼리를 커버링 인덱스로 해결할 수 있음을 나타낸다.<br/>보통의 `GROUP BY` 절은 그룹핑 기준 컬럼을 이용해 정렬 작업을 수행하고 다시 정렬된 결과를 그룹핑하는 형태의 부하 작업을 필요하다. <br/>하지만 `GROUP BY` 처리가 인덱스를 이용하면 정렬된 인덱스 컬럼을 순서대로 읽으면서 그룹핑 작업만 수행하면 됩니다. 이렇게 인덱스를 이용하면 `row`의 정렬이 필요로 하지 않고 인덱스의 필요한 부분만 읽으면 되기 때문에 상당히 효율적이고 빠르게 처리됩니다. `GROUP BY` 처리를 위해 인덱스를 읽는 방법을 "루스 인덱스 스캔"이라고 한다. |
| Using filesort                        | `ORDER BY` 인덱스로 해결하지 못하고, `filesort`(MySQL의 quick sort)로 행을 정렬한 것을 나타낸다.                                                                                                                                                                                                                                                                               |
| Using temporary                       | 암묵적으로 임시 테이블이 생성된 것을 표시한다.<br/>쿼리를 처리하는 동안 중간 결과를 담아두기 위해 임시 테이블(Temporary table)을 사용하면 이 키워드가 표시된다. <br/>임시 테이블은 메모리 상에 생성될 수도 있고, 디스크 상에 생성될 수도 있다. <br/>이때 사용된 임시 테이블이 메모리에 생성됐는지, 디스크에 생성됐는지는 실행 계획만으로는 알 수 없다.                                                                                                                                |
| Using where with pushed               | 엔진 컨디션 pushdown 최적화가 일어난 것을 표시한다. 현재는 NDB만 유효                                                                                                                                                                                                                                                                                                      |
| Using index condition                 | 인덱스 컨디션 pushdown(ICP) 최적화가 일어났음을 표시한다. ICP는 멀티 칼럼 인덱스에서 왼쪽부터 순서대로 칼럼을 지정하지 않는 경우에도 인덱스를 이용하는 실행 계획이다.                                                                                                                                                                                                                                              |
| Using MRR                             | 멀티 레인지 리드(MRR) 최적화가 사용되었음을 표시한다.                                                                                                                                                                                                                                                                                                                   |
| Using join buffer(Block Nested Loop)  | 조인에 적절한 인덱스가 없어 조인 버퍼를 이용했음을 표시한다.                                                                                                                                                                                                                                                                                                                 |
| Using join buffer(Batched Key Access) | Batched Key Access(BKAJ) 알고리즘을 위한 조인 버퍼를 사용했음을 표시한다.                                                                                                                                                                                                                                                                                               |

## Reference

- [dev.mysql.com - MySQL 8.0 EXPLAIN Statement](https://dev.mysql.com/doc/refman/8.0/en/explain.html)
- [dev.mysql.com - MySQL 8.0 EXPLAIN Output Format](https://dev.mysql.com/doc/refman/8.0/en/explain-output.html)
- [dev.mysql.com - MySQL 8.0 Explain Extra Information](https://dev.mysql.com/doc/refman/8.0/en/explain-output.html#explain-extra-information)
- [icarus8050.tistory.com - MySQL Explain 실행 계획](https://icarus8050.tistory.com/45)
- [cheese10yun.github.io - MySQL Explain](https://cheese10yun.github.io/mysql-explian/)
