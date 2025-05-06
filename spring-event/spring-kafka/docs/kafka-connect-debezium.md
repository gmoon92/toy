# Kafka Connect와 Debezium을 활용한 Outbox 패턴 구현

Kafka Connect는 Kafka와 외부 시스템을 쉽게 연동할 수 있도록 도와주는 프레임워크다. 다양한 프로토콜과 저장소와의 연동을 지원하며, 대표적으로 MySQL, PostgreSQL, MongoDB, Elasticsearch, S3 등과 연결할 수 있다.

이 문서는 Kafka Connect와 Debezium을 이용하여 Outbox 패턴을 구현하는 방법을 설명하고, 관련 설정과 실습 예제를 정리한 자료다.

## Kafka Connect 기본 용어

Kafka Connect에서 사용하는 주요 용어를 간단히 정리하면 다음과 같다.

- **소스 커넥터 (source connector)**: 외부 시스템에서 Kafka로 데이터를 가져오는 역할을 한다. 예: MySQL → Kafka
- **싱크 커넥터 (sink connector)**: Kafka에서 외부 시스템으로 데이터를 내보내는 역할을 한다. 예: Kafka → Elasticsearch

## Debezium이란?

Debezium은 오픈소스 CDC(Change Data Capture) 라이브러리로, 데이터베이스 변경 사항을 Kafka 이벤트로 발행해준다. Debezium은 단일 소스 커넥터만 지원하기 때문에 이벤트의 순서를 보장하는 데 유리하다.

## Debezium 사용을 위한 MySQL 설정

Debezium은 MySQL의 바이너리 로그(Binlog)를 기반으로 동작하므로 다음 설정이 필요하다.

### [1. Binlog 활성화]((https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-mysql-binlog))

```sql
-- MySQL 5.x
SELECT variable_value as "BINARY LOGGING STATUS (log-bin) ::"
FROM information_schema.global_variables
WHERE variable_name='log_bin';

-- MySQL 8.x
SELECT variable_value as "BINARY LOGGING STATUS (log-bin) ::"
FROM performance_schema.global_variables
WHERE variable_name='log_bin';
```

결과가 `ON`이면 활성화된 것이다.

### [2. GTID(Global Transaction Identifier) 활성화 (선택)]((https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-mysql-gtids))

Debezium은 GTID 기반 복제를 지원한다. 다음 설정을 적용하면 된다.

```sql
SET GLOBAL gtid_mode = ON;
SET GLOBAL enforce_gtid_consistency = ON;

SHOW GLOBAL VARIABLES LIKE '%GTID%';
```

## 커넥터 등록 예시 (MySQL 기준)

다음은 커넥터를 등록할 때 사용하는 설정의 예다.

```json
"database.include.list": "inventory",
"table.include.list": "inventory.outbox",
"include.schema.changes": false
```

- `database.include.list`: 캡처 대상 데이터베이스 지정
- `table.include.list`: 캡처 대상 테이블 지정
- `include.schema.changes`: DDL 이벤트 포함 여부 (false로 설정하면 DML만 수신함)

## Debezium Outbox Event Router

Debezium Outbox Event Router는 Outbox 패턴을 위한 Single Message Transformation(SMT)이다. 하나의 Outbox 테이블에서 이벤트를 추출하여 집합체(aggregate)별로 Kafka 토픽에 라우팅할 수 있다.

### Outbox 테이블 구성 예시

```sql
CREATE TABLE outbox_message (
  id              VARCHAR(36) PRIMARY KEY,
  aggregate_type  VARCHAR(255),
  aggregate_id    VARCHAR(255),
  type            VARCHAR(255),
  payload         JSON,
  created_at      TIMESTAMP
);
```

### Kafka 메시지 예시

```text
Kafka Topic: outbox.event.order
Kafka Message Key: "1"
Kafka Message Headers: "id=4d47e190..."
Kafka Message Timestamp: 1556890294484
Kafka Message Payload: {"id": 1, "lineItems": [...], "orderDate": ..., "customerId": 123}
```

## 주요 구성 옵션 요약

| 옵션 이름                                           | 설명                                                     |
| ----------------------------------------------- | ------------------------------------------------------ |
| `transforms.outbox.route.by.field.name`         | 라우팅 기준 필드 설정 (예: aggregate\_type)                      |
| `transforms.outbox.route.topic.replacement`     | 라우팅할 토픽 이름 패턴 설정 (예: outbox.event.\${aggregate\_type}) |
| `transforms.outbox.route.key.field.name`        | Kafka 메시지 키로 사용할 필드 설정 (예: aggregate\_id)              |
| `transforms.outbox.table.field.event.timestamp` | Kafka 메시지 타임스탬프로 사용할 필드 설정 (예: created\_at)            |
| `transforms.outbox.table.expand.json.payload`   | JSON 문자열을 확장하여 Kafka 메시지의 필드로 포함할지 여부                  |

## SMT 설정 예시

```json
{
  "name": "debezium-mysql-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "database.hostname": "mysql",
    "database.port": "3306",
    "database.user": "root",
    "database.password": "111111",

    "database.server.id": "184054",
    "topic.prefix": "outbox",

    "schema.history.internal.kafka.bootstrap.servers": "broker:19092",
    "schema.history.internal.kafka.topic": "schemahistory.spring_kafka",

    "include.schema.changes": "false",
    "database.include.list": "spring_kafka",
    "table.include.list": "spring_kafka.outbox_message",

    "tombstones.on.delete": "true",

    "transforms": "outbox",
    "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
    "transforms.outbox.table.field.event.key": "aggregate_id",
    "transforms.outbox.route.by.field": "aggregate_type",
    "transforms.outbox.table.field.event.timestamp": "created_at",
    "transforms.outbox.table.field.event.payload": "payload",
    "transforms.outbox.table.expand.json.payload": "true",
    "transforms.outbox.table.fields.additional.placement": "event_type:header,created_at:envelope",

    "value.converter": "org.apache.kafka.connect.json.JsonConverter"
  }
}
```

### 커넥터 등록 방법

Kafka Connect는 커넥터 설정을 REST API를 통해 등록할 수 있도록 지원한다. 위의 JSON 설정 파일을 등록하려면 다음과 같은 curl 명령어를 사용할 수 있다:

```bash
curl -X POST -H "Content-Type: application/json" \
  --data @register-mysql-outbox.json \
  http://localhost:8083/connectors
```

- @register-mysql-outbox.json: 등록할 커넥터 설정이 담긴 JSON 파일 경로
- localhost:8083: Kafka Connect REST API가 열려 있는 호스트와 포트

## Reference

- [Debezium - Source connect](https://debezium.io/documentation/reference/stable/connectors/index.html)
    - [Reliable Microservices Data Exchange With the Outbox Pattern](https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/)
    - [Source connect MySQL](https://debezium.io/documentation/reference/stable/connectors/mysql.html)
        - [Data change events](https://debezium.io/documentation/reference/stable/connectors/mysql.html#mysql-events)
    - [Source connect Mariadb](https://debezium.io/documentation/reference/stable/connectors/mariadb.html)
    - [Outbox Event Router](https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html)
        - [Applying transformations selectively](https://debezium.io/documentation/reference/stable/transformations/applying-transformations-selectively.html?utm_source=chatgpt.com)
        - [MongoDB Outbox Event Router](https://debezium.io/blog/2021/11/30/debezium-1.8-beta1-released/?utm_source=chatgpt.com)
- [Confluent - Connect](https://docs.confluent.io/platform/current/connect/index.html)
    - [Confluent - Event Sourcing pattern explained](https://developer.confluent.io/courses/microservices/the-transactional-outbox-pattern/)
    - [Confluent - Kafka Connect Deep Dive – Converters and Serialization Explained](https://www.confluent.io/blog/kafka-connect-deep-dive-converters-serialization-explained/)
- [Apache Kafka - Connect](https://kafka.apache.org/documentation.html#connect)
    - [Quickstart Connect](https://kafka.apache.org/quickstart#quickstart_kafkaconnect)
    - [Connect configs](https://kafka.apache.org/documentation.html#connectconfigs)
- [microservices.io - transaction log tailing](https://microservices.io/patterns/data/transaction-log-tailing.html)
