# Kafka Transactional Outbox Pattern

# Kafka Connector

카프카 커넥터(Connector)는 카프카와 외부 시스템을 연동 시 쉽게 연동 가능하도록 하는 프레임워크로 MySQL, S3 등 다양한 프로토콜과 연동을 지원합니다.

- 소스커넥터(source connector): 메시지 발행과 관련 있는 커넥터
- 싱크커넥터(sink connector): 메시지 소비와 관련 있는 커넥터

# Debezium

CDC (Change Data Capture) 라이브러리로 단일 소스 커넥터만 지원하기 때문에 메시지 순서 보장된다.

```
curl -X POST 127.0.0.1:8083/connectors
```

## Reference

- [Confluent - Connect](https://docs.confluent.io/platform/current/connect/index.html)
- [Apache Kafka - Connect](https://kafka.apache.org/documentation.html#connect)
  - [Apache Kafka - Quickstart Connect](https://kafka.apache.org/quickstart#quickstart_kafkaconnect)
- [Debezium - Source connect mariadb](https://debezium.io/documentation/reference/stable/connectors/mariadb.html)
- [microservices.io - transaction log tailing](https://microservices.io/patterns/data/transaction-log-tailing.html)
