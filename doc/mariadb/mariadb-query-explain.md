# Explain

MariaDB 쿼리 최적화에 있어 EXPLAIN 키워드와 실행 계획에 대해 정리한다.

## Syntax

EXPLAIN 은 [DESCRIBE](https://mariadb.com/kb/en/describe/) 와 동의어로 사용할 수 있다.

- EXPLAIN `table_name [column_name | wild]`
-
EXPLAIN `[EXTENDED | PARTITIONS | FORMAT=JSON] {SELECT select_options | UPDATE update_options | DELETE delete_options}`
  - EXTENDED: 실행 계획에 `filtered` 항목이 추가되어 보여준다. (11 versions)
  - [PARTITIONS](https://mariadb.com/kb/en/partition-pruning-and-selection/): 파티션 테이블과 관련된 쿼리를 검사할 때 사용한다.
  - FORMAT=JSON: 실행 계획을 JSON 형식으로 보여준다.
- [ANALYZE](https://mariadb.com/kb/en/analyze-statement/) 키워드를 사용하게 되면, 실행 계획과 쿼리에 대한 실제 통계와 추정 통계를 확인할 수 있다.

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

-- EXPLAIN table_name {SELECT ... | UPDATE ... | DELETE ...}
EXPLAIN
SELECT *
FROM ex_access_log;
DESCRIBE
SELECT *
FROM ex_access_log;
/**
+--+-----------+-------------+----+-------------+----+-------+----+----+-----+
|id|select_type|table        |type|possible_keys|key |key_len|ref |rows|Extra|
+--+-----------+-------------+----+-------------+----+-------+----+----+-----+
|1 |SIMPLE     |ex_access_log|ALL |null         |null|null   |null|5782|     |
+--+-----------+-------------+----+-------------+----+-------+----+----+-----+
**/

EXPLAIN EXTENDED SELECT * FROM ex_access_log;
DESCRIBE EXTENDED
SELECT *
FROM ex_access_log;
/**
+--+-----------+-------------+----+-------------+----+-------+----+----+--------+-----+
|id|select_type|table        |type|possible_keys|key |key_len|ref |rows|filtered|Extra|
+--+-----------+-------------+----+-------------+----+-------+----+----+--------+-----+
|1 |SIMPLE     |ex_access_log|ALL |null         |null|null   |null|5782|100     |     |
+--+-----------+-------------+----+-------------+----+-------+----+----+--------+-----+
**/

EXPLAIN FORMAT=JSON SELECT * FROM ex_access_log;
DESCRIBE FORMAT=JSON
SELECT *
FROM ex_access_log;
/**
{ "query_block": { "select_id": 1, "cost": 1.0353816, "nested_loop": [ { "table": { "table_name": "ex_access_log", "access_type": "ALL", "loops": 1, "rows": 5782, "cost": 1.0353816, "filtered": 100 } } ] } }
**/

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

|               | Description                                                      |
|---------------|------------------------------------------------------------------|
| id            | 테이블이 조인되는 순서를 나타내는 시퀀스 번호                                        |
| select_type   | 테이블을 접근하기 위해 사용한 SELECT 문의 유형                                    |
| table         | 참조하는 테이블<br/>테이블의 별칭 또는 하위 쿼리에 대한 구체화된 임시 테이블은 `<subquery#>`로 표시 |
| type          | 테이블에서 행을 찾는 방법 또는 조인 유형                                          |
| possible_keys | 테이블의 행을 찾는 데 사용할 수 있는 테이블의 키                                     |
| key           | 행을 검색하는 데 사용되는 키의 이름 <br/>키를 사용하지 못할 경우 `NULL` 로 표시              |
| key_len       | 사용된 키의 바이트 수(multi-column 키인 경우 사용된 키에 대해서만 일부만 표시)              |
| ref           | key 컬럼에 지정된 인덱스가 참조 조건으로 어떤 컬럼이 제공되었는지 나타낸다.                     |
| rows          | 각 키 조회에서 테이블에서 찾을 수 있는 행의 수에 대한 추정치입니다.                          |
| Extra         | 조인에 대한 추가 정보                                                     |

## Select_type

## Reference

- [www.mariadb.com - Knowledge Base](https://mariadb.com/kb/en/)
  - [EXPLAIN](https://mariadb.com/kb/en/explain/)
  - [DESCRIBE](https://mariadb.com/kb/en/describe/)
  - [Partition Pruning and Selection](https://mariadb.com/kb/en/partition-pruning-and-selection/)
