# Kafka Connect Debezium

카프카 커넥터(Connector)는 카프카와 외부 시스템을 연동 시 쉽게 연동 가능하도록 하는 프레임워크로 MySQL, S3 등 다양한 프로토콜과 연동을 지원한다.

## Kafka Connect 관련 용어 사전

들어가기 전 카프카 용어를 먼저 정리하고자 한다.

- 소스커넥터(source connector): 메시지 발행과 관련 있는 커넥터
- 싱크커넥터(sink connector): 메시지 소비와 관련 있는 커넥터

# Debezium

CDC (Change Data Capture) 라이브러리로 단일 소스 커넥터만 지원하기 때문에 메시지 순서 보장된다.

# REST API

```
curl -X POST 127.0.0.1:8083/connectors
```


https://debezium.io/documentation/reference/stable/connectors/mysql.html#mysql-example-configuration


### [binlog 활성화](https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-mysql-binlog)

```text
# root 접속
mysql -u root -p111111

// for MySQL 5.x
mysql> SELECT variable_value as "BINARY LOGGING STATUS (log-bin) ::" FROM information_schema.global_variables WHERE variable_name='log_bin';
// for MySQL 8.x
mysql> SELECT variable_value as "BINARY LOGGING STATUS (log-bin) ::" FROM performance_schema.global_variables WHERE variable_name='log_bin';

mysql> SELECT variable_value as "BINARY LOGGING STATUS (log-bin) ::" FROM performance_schema.global_variables WHERE variable_name='log_bin';
+------------------------------------+
| BINARY LOGGING STATUS (log-bin) :: |
+------------------------------------+
| ON                                 |
+------------------------------------+
1 row in set (0.02 sec)

```

## [MySQL GTID 활성화](https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-mysql-gtids)

```text
mysql> gtid_mode=ON
mysql> enforce_gtid_consistency=ON

mysql> show global variables like '%GTID%';
+----------------------------------+-------+
| Variable_name                    | Value |
+----------------------------------+-------+
| binlog_gtid_simple_recovery      | ON    |
| enforce_gtid_consistency         | OFF   |
| gtid_executed                    |       |
| gtid_executed_compression_period | 0     |
| gtid_mode                        | OFF   |
| gtid_owned                       |       |
| gtid_purged                      |       |
| session_track_gtids              | OFF   |
+----------------------------------+-------+
8 rows in set (0.01 sec)

```

## Reference

- [Debezium - Source connect](https://debezium.io/documentation/reference/stable/connectors/index.html)
  - [Reliable Microservices Data Exchange With the Outbox Pattern](https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/)
  - [Source connect MySQL](https://debezium.io/documentation/reference/stable/connectors/mysql.html)
  - [Source connect Mariadb](https://debezium.io/documentation/reference/stable/connectors/mariadb.html)
- [Confluent - Connect](https://docs.confluent.io/platform/current/connect/index.html)
  - [Confluent - Event Sourcing pattern explained](https://developer.confluent.io/courses/microservices/the-transactional-outbox-pattern/)
- [Apache Kafka - Connect](https://kafka.apache.org/documentation.html#connect)
  - [Quickstart Connect](https://kafka.apache.org/quickstart#quickstart_kafkaconnect)
  - [Connect configs](https://kafka.apache.org/documentation.html#connectconfigs)
- [microservices.io - transaction log tailing](https://microservices.io/patterns/data/transaction-log-tailing.html)
