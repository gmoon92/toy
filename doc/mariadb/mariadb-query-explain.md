# Explain

MariaDB 쿼리 최적화에 있어 EXPLAIN 키워드와 실행 계획에 대해 정리한다.

## Syntax

EXPLAIN 은 [DESCRIBE](https://mariadb.com/kb/en/describe/) 와 동의어로 사용할 수 있다.

- EXPLAIN `table_name [column_name | wild]`
  ```sql
  -- EXPLAIN table_name [column_name | wild]
  EXPLAIN ex_access_log;
  DESCRIBE ex_access_log;
  SHOW COLUMNS FROM ex_access_log;
  /**
  +----------+------------+----+---+-------+-----+
  |Field     |Type        |Null|Key|Default|Extra|
  +----------+------------+----+---+-------+-----+
  |id        |varchar(50) |NO  |PRI|null   |     |
  |username  |varchar(50) |NO  |   |null   |     |
  |ip        |varchar(100)|NO  |   |null   |     |
  |os        |varchar(30) |NO  |   |null   |     |
  |attempt_dt|varchar(50) |NO  |   |null   |     |
  +----------+------------+----+---+-------+-----+
  **/
  ```
- EXPLAIN `[EXTENDED | PARTITIONS | FORMAT=JSON] {SELECT select_options | UPDATE update_options | DELETE delete_options}`
  ```sql
  -- EXPLAIN table_name {SELECT ... | UPDATE ... | DELETE ...}
  EXPLAIN SELECT *FROM ex_access_log;
  DESCRIBE SELECT *FROM ex_access_log;
  /**
  +--+-----------+-------------+----+-------------+----+-------+----+----+-----+
  |id|select_type|table        |type|possible_keys|key |key_len|ref |rows|Extra|
  +--+-----------+-------------+----+-------------+----+-------+----+----+-----+
  |1 |SIMPLE     |ex_access_log|ALL |null         |null|null   |null|5782|     |
  +--+-----------+-------------+----+-------------+----+-------+----+----+-----+
  **/
  EXPLAIN EXTENDED SELECT * FROM ex_access_log;
  DESCRIBE EXTENDED SELECT *FROM ex_access_log;
  /**
  +--+-----------+-------------+----+-------------+----+-------+----+----+--------+-----+
  |id|select_type|table        |type|possible_keys|key |key_len|ref |rows|filtered|Extra|
  +--+-----------+-------------+----+-------------+----+-------+----+----+--------+-----+
  |1 |SIMPLE     |ex_access_log|ALL |null         |null|null   |null|5782|100     |     |
  +--+-----------+-------------+----+-------------+----+-------+----+----+--------+-----+
  **/
  
  EXPLAIN FORMAT=JSON SELECT * FROM ex_access_log;
  DESCRIBE FORMAT=JSON SELECT *FROM ex_access_log;
  /**
  { "query_block": { "select_id": 1, "cost": 1.0353816, "nested_loop": [ { "table": { "table_name": "ex_access_log", "access_type": "ALL", "loops": 1, "rows": 5782, "cost": 1.0353816, "filtered": 100 } } ] } }
  **/
  ```
  - EXTENDED: 실행 계획에 `filtered` 항목이 추가되어 보여준다. (11 versions)
  - [PARTITIONS](https://mariadb.com/kb/en/partition-pruning-and-selection/): 파티션 테이블과 관련된 쿼리를 검사할 때 사용한다.
  - FORMAT=JSON: 실행 계획을 JSON 형식으로 보여준다.
- [ANALYZE](https://mariadb.com/kb/en/analyze-statement/) 키워드를 사용하게 되면, 실행 계획과 쿼리에 대한 실제 통계와 추정 통계를 확인할 수 있다.
  ```sql
  ANALYZE SELECT * FROM ex_access_log;
  /**
  +--+-----------+-------------+----+-------------+----+-------+----+----+-------+--------+----------+-----+
  |id|select_type|table        |type|possible_keys|key |key_len|ref |rows|r_rows |filtered|r_filtered|Extra|
  +--+-----------+-------------+----+-------------+----+-------+----+----+-------+--------+----------+-----+
  |1 |SIMPLE     |ex_access_log|ALL |null         |null|null   |null|5782|6006.00|100     |100       |     |
  +--+-----------+-------------+----+-------------+----+-------+----+----+-------+--------+----------+-----+
  **/
  ```

## EXPLAIN

|                                 | Description                                                      |
|---------------------------------|------------------------------------------------------------------|
| [id](#id)                       | 테이블이 조인되는 순서를 나타내는 시퀀스 번호                                        |
| [select_type](#select_type)     | 테이블을 접근하기 위해 사용한 SELECT 문의 유형                                    |
| [table](#table)                 | 참조하는 테이블<br/>테이블의 별칭 또는 하위 쿼리에 대한 구체화된 임시 테이블은 `<subquery#>`로 표시 |
| [type](#type)                   | 테이블에서 행을 찾는 방법 또는 조인 유형                                          |
| [possible_keys](#possible_keys) | 테이블의 행을 찾는 데 사용할 수 있는 테이블의 키                                     |
| [key](#key)                     | 행을 검색하는 데 사용되는 키의 이름 <br/>키를 사용하지 못할 경우 `NULL` 로 표시              |
| [key_len](#key_len)             | 사용된 키의 바이트 수(multi-column 키인 경우 사용된 키에 대해서만 일부만 표시)              |
| [ref](#ref)                     | key 컬럼에 지정된 인덱스가 참조 조건으로 어떤 컬럼이 제공되었는지 나타낸다.                     |
| [rows](#rows)                   | 각 키 조회에서 테이블에서 찾을 수 있는 행의 수에 대한 추정치입니다.                          |
| [Extra](#Extra)                 | 조인에 대한 추가 정보                                                     |

### `id`

쿼리에 대한 **실행 단위**를 식별하기 위한 시퀀스 번호다.

MariaDB는 `JOIN`을 **하나의 실행 단위**로 인식한다.

```sql
EXPLAIN EXTENDED
SELECT A.attempt_dt
FROM ex_access_log AS A -- id=1
WHERE A.username = 'admin'
UNION
SELECT B.attempt_dt
FROM ex_access_log AS B -- id=2
       JOIN ex_access_log C ON B.id = C.id -- id2
WHERE B.os = 'WINDOW'
  AND B.username IN (SELECT sub_log.username
                     FROM ex_access_log AS sub_log -- id=3
                     WHERE sub_log.username = 'admin');
```

- 하나의 `SELECT` 문에서 여러 개의 테이블을 조인하면 조인되는 테이블의 개수만큼 실행 계획 `row`가 출력되지만 같은 `id`가 부여된다.
- 만약 쿼리안에 `Sub Query`, `UNION`과 같은 서로 다른 실행 단위로 구성되어 있다면, 각 `SELECT`의 `id`는 다른 값으로 부여된다.

```text
+----+------------+-----------+------+-------------+------------+-------+----------------+----+--------+-----------+
|id  |select_type |table      |type  |possible_keys|key         |key_len|ref             |rows|filtered|Extra      |
+----+------------+-----------+------+-------------+------------+-------+----------------+----+--------+-----------+
|1   |PRIMARY     |A          |ALL   |null         |null        |null   |null            |5782|100     |Using where|
|2   |UNION       |B          |ALL   |PRIMARY      |null        |null   |null            |5782|100     |Using where|
|2   |UNION       |C          |eq_ref|PRIMARY      |PRIMARY     |152    |batchinsert.B.id|1   |100     |           |
|2   |UNION       |<subquery3>|eq_ref|distinct_key |distinct_key|153    |func            |1   |100     |           |
|3   |MATERIALIZED|sub_log    |ALL   |null         |null        |null   |null            |5782|100     |Using where|
|null|UNION RESULT|<union1,2> |ALL   |null         |null        |null   |null            |null|null    |           |
+----+------------+-----------+------+-------------+------------+-------+----------------+----+--------+-----------+
```

### `select_type`

- `SIMPLE`: `Sub Query` 또는 `UNION` 이 없는 간단한 SELECT 문.
- `PRIMARY`: 가장 바깥의 SELECT 문
  - Sub Query를 사용할 경우 Sub Query의 외부에 있는 첫 번째 쿼리
  - UNION 인 경우엔 UNION의 첫 번째 SELECT 쿼리
- `SUBQUERY`: 첫 번째 SELECT 문의 Sub Query
- `DEPENDENT SUBQUERY`: 외부 쿼리에 의존적인 Sub Query
  - 대표적으로 스칼라 서브 쿼리
  - Sub Query가 먼저 실행되지 못하고 Sub Query가 외부 쿼리 결과에 의존적이기 때문에 전체 쿼리의 성능을 느리게 만든다. 
  - Sub Query가 "외부 쿼리"의 값을 전달받고 있는지 검토해서, 가능하다면 "외부 쿼리"와의 의존도를 제거하는 것이 좋다.
- `UNCACHEABLE SUBQUERY`: 캐싱하지 못하는 Sub Query 
  - Sub Query는 종류에 따라 외부 쿼리 row 수만큼 수행되어야 하는 경우도 있다.
  - 실제로 그렇게 작동한다면 성능에 큰 영향을 끼치게 되므로 때에 따라 쿼리를 캐싱해놓고 캐싱 된 데이터를 갖다 쓰게끔 최적화가 되어있는데 그런 캐싱이 작동할 수 없는 경우에 표현된다. 
- `UNION`: UNION 문에서 `PRIMARY` 유형을 제외한 나머지 SELECT
- `UNION RESULT`: UNION 결과
- `DEPENDENT UNION`: 외부 쿼리에 의존적인 UNION
  - UNION으로 결합된 단위 쿼리가 바깥쪽 쿼리에 의존적이어서 외부의 영향을 받고 있는 경우
- `UNCACHEABLE UNION`: 캐싱하지 못하는 UNION Sub Query
- `DERIVED`: `Derived table`
  - 인라인 뷰(FROM 절에 사용된 Sub Query)로 부터 발생한 임시 테이블을 의미한다.
  - 임시 테이블은 메모리에 저장될 수도 있고, 디스크에 저장될 수도 있다. 
  - 일반적으로 메모리에 저장하는 경우에는 성능에 큰 영향을 미치지 않지만, 데이터의 크기가 커서 임시 테이블을 디스크에 저장할 경우 성능이 떨어지게 된다.
- `LATERAL DERIVED`: [Lateral Derived optimization](https://mariadb.com/kb/en/lateral-derived-optimization/) 사용할 경우
  - Derived 테이블 또는 VIEW 테이블의 키 컬럼과 동등 비교시 최적화를 수행하기 위한 생성된 임시 테이블을 의미
  - optimizer_switch = 'split_materialized=off' 로 활성화시 옵티마이저 최적화 수행
- `MATERIALIZED`: MySQL 5.6 버전에 추가된 유형으로 IN 절 내의 Sub Query를 임시 테이블로 만들어 조인을 하는 형태로 최적화 해준다. DERIVED 와 비슷한 개념이다.
  - optimizer_switch 'semijoin=on', 'materialization=on' 활성화되어야만 [세미조인 MATERIALIZED 최적화](https://mariadb.com/kb/en/semi-join-materialization-strategy/) 수행. 

### `table`
### `type`
### `possible_keys`
### `key`
### `key_len`
### `ref`
### `rows`
### `Extra`

## Reference

- [www.mariadb.com - Knowledge Base](https://mariadb.com/kb/en/)
  - [EXPLAIN](https://mariadb.com/kb/en/explain/)
  - [DESCRIBE](https://mariadb.com/kb/en/describe/)
  - [Partition Pruning and Selection](https://mariadb.com/kb/en/partition-pruning-and-selection/)
