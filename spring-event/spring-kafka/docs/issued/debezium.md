# Kafka Connect Debezium





```
2025-04-14 05:47:01,266 WARN   ||  [Producer clientId=outbox-schemahistory] Connection to node 1 (localhost/127.0.0.1:9092) could not be established. Node may not be available.   [org.apache.kafka.clients.NetworkClient]
2025-04-14T05:47:01.613393233Z 2025-04-14 05:47:01,612 ERROR  MySQL|outbox|snapshot  Error during snapshot   [io.debezium.relational.RelationalSnapshotChangeEventSource]
2025-04-14T05:47:01.613450774Z io.debezium.relational.history.SchemaHistoryException: java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.TimeoutException: Topic db.schema.changed not present in metadata after 10000 ms.
```


카프카 커넥터(Connector)는 카프카와 외부 시스템을 연동 시 쉽게 연동 가능하도록 하는 프레임워크로 MySQL, S3 등 다양한 프로토콜과 연동을 지원한다.

## Kafka Connect 관련 용어 사전

들어가기 전 카프카 용어를 먼저 정리하고자 한다.

- 소스커넥터(source connector): 메시지 발행과 관련 있는 커넥터
- 싱크커넥터(sink connector): 메시지 소비와 관련 있는 커넥터

# Debezium

CDC (Change Data Capture) 라이브러리로 단일 소스 커넥터만 지원하기 때문에 메시지 순서 보장된다.

```
curl -X POST 127.0.0.1:8083/connectors
```

```text
2025-04-11 07:58:10,406 WARN   ||  [AdminClient clientId=adminclient-1] Connection to node -1 (localhost/127.0.0.1:19092) could not be established. Node may not be available.   [org.apache.kafka.clients.NetworkClient]
2025-04-11T07:58:11.441360012Z 2025-04-11 07:58:11,436 INFO   ||  [AdminClient clientId=adminclient-1] Node -1 disconnected.   [org.apache.kafka.clients.NetworkClient]
2025-04-11T07:58:11.441465929Z 2025-04-11 07:58:11,437 WARN   ||  [AdminClient clientId=adminclient-1] Connection to node -1 (localhost/127.0.0.1:19092) could not be established. Node may not be available.   [org.apache.kafka.clients.NetworkClient]
2025-04-11T07:58:12.173325179Z 2025-04-11 07:58:12,171 INFO   ||  [AdminClient clientId=adminclient-1] Metadata update failed   [org.apache.kafka.clients.admin.internals.AdminMetadataManager]
2025-04-11T07:58:12.173370346Z org.apache.kafka.common.errors.TimeoutException: Timed out waiting for a node assignment. Call: fetchMetadata
2025-04-11T07:58:12.178183804Z 2025-04-11 07:58:12,177 INFO   ||  App info kafka.admin.client for adminclient-1 unregistered   [org.apache.kafka.common.utils.AppInfoParser]
2025-04-11T07:58:12.178453888Z 2025-04-11 07:58:12,178 INFO   ||  [AdminClient clientId=adminclient-1] Metadata update failed   [org.apache.kafka.clients.admin.internals.AdminMetadataManager]
2025-04-11T07:58:12.178584679Z org.apache.kafka.common.errors.TimeoutException: The AdminClient thread has exited. Call: fetchMetadata
2025-04-11T07:58:12.178751763Z 2025-04-11 07:58:12,178 INFO   ||  [AdminClient clientId=adminclient-1] Timed out 1 remaining operation(s) during close.   [org.apache.kafka.clients.admin.KafkaAdminClient]
2025-04-11T07:58:12.189563929Z 2025-04-11 07:58:12,188 INFO   ||  Metrics scheduler closed   [org.apache.kafka.common.metrics.Metrics]
2025-04-11T07:58:12.189840013Z 2025-04-11 07:58:12,189 INFO   ||  Closing reporter org.apache.kafka.common.metrics.JmxReporter   [org.apache.kafka.common.metrics.Metrics]
2025-04-11T07:58:12.190028638Z 2025-04-11 07:58:12,189 INFO   ||  Metrics reporters closed   [org.apache.kafka.common.metrics.Metrics]
2025-04-11T07:58:12.193162679Z 2025-04-11 07:58:12,190 ERROR  ||  Stopping due to error   [org.apache.kafka.connect.cli.AbstractConnectCli]
2025-04-11T07:58:12.193171388Z org.apache.kafka.connect.errors.ConnectException: Failed to connect to and describe Kafka cluster. Check worker's broker connection and security properties.
2025-04-11T07:58:12.193186513Z 	at org.apache.kafka.connect.runtime.WorkerConfig.lookupKafkaClusterId(WorkerConfig.java:305)
2025-04-11T07:58:12.193189429Z 	at org.apache.kafka.connect.runtime.WorkerConfig.lookupKafkaClusterId(WorkerConfig.java:285)
2025-04-11T07:58:12.193191429Z 	at org.apache.kafka.connect.runtime.WorkerConfig.kafkaClusterId(WorkerConfig.java:415)
2025-04-11T07:58:12.193193929Z 	at org.apache.kafka.connect.cli.AbstractConnectCli.startConnect(AbstractConnectCli.java:124)
2025-04-11T07:58:12.193195846Z 	at org.apache.kafka.connect.cli.AbstractConnectCli.run(AbstractConnectCli.java:94)
2025-04-11T07:58:12.193197888Z 	at org.apache.kafka.connect.cli.ConnectDistributed.main(ConnectDistributed.java:113)
2025-04-11T07:58:12.193199846Z Caused by: java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.TimeoutException: Timed out waiting for a node assignment. Call: listNodes
2025-04-11T07:58:12.193202013Z 	at java.base/java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:396)
2025-04-11T07:58:12.193204013Z 	at java.base/java.util.concurrent.CompletableFuture.get(CompletableFuture.java:2073)
2025-04-11T07:58:12.193205929Z 	at org.apache.kafka.common.internals.KafkaFutureImpl.get(KafkaFutureImpl.java:165)
2025-04-11T07:58:12.193207846Z 	at org.apache.kafka.connect.runtime.WorkerConfig.lookupKafkaClusterId(WorkerConfig.java:299)
2025-04-11T07:58:12.193620013Z 	... 5 more
2025-04-11T07:58:12.193625054Z Caused by: org.apache.kafka.common.errors.TimeoutException: Timed out waiting for a node assignment. Call: listNodes

```


```text
#


### One or more plugins are missing ServiceLoader manifests may not be usable with plugin.discovery=service_load

Kafka 버전 3.6 이상에서 기본 plugin.discovery 워커 구성을 사용하여 커넥터를 사용하면 경고가 발생합니다.

connect +3.0.0 이슈

- https://github.com/ClickHouse/clickhouse-kafka-connect/pull/351
- https://github.com/ClickHouse/clickhouse-kafka-connect/issues/350
- https://kafka.apache.org/documentation.html#connectconfigs_plugin.discovery

```text
kafka-connect: 2025-04-11 04:09:55,553 WARN   ||  One or more plugins are missing ServiceLoader manifests may not be usable with plugin.discovery=service_load: [
kafka-connect: file:/kafka/connect/debezium-connector-mongodb/	io.debezium.connector.mongodb.MongoDbSinkConnector	sink	3.0.0.Final
kafka-connect: file:/kafka/connect/debezium-connector-postgres/	io.debezium.connector.postgresql.transforms.DecodeLogicalDecodingMessageContent	transformation	3.0.0.Final
kafka-connect: file:/kafka/connect/debezium-connector-vitess/	io.debezium.connector.vitess.transforms.FilterTransactionTopicRecords	transformation	3.0.0.Final
kafka-connect: file:/kafka/connect/debezium-connector-vitess/	io.debezium.connector.vitess.transforms.RemoveField	transformation	3.0.0.Final
kafka-connect: file:/kafka/connect/debezium-connector-vitess/	io.debezium.connector.vitess.transforms.UseLocalVgtid	transformation	3.0.0.Final
kafka-connect: ]
```


````text
2025-04-11 07:58:10,405 INFO   ||  [AdminClient clientId=adminclient-1] Node -1 disconnected.   [org.apache.kafka.clients.NetworkClient]
2025-04-11T07:58:10.406784345Z 2025-04-11 07:58:10,406 WARN   ||  [AdminClient clientId=adminclient-1] Connection to node -1 (localhost/127.0.0.1:19092) could not be established. Node may not be available.   [org.apache.kafka.clients.NetworkClient]
2025-04-11T07:58:11.441360012Z 2025-04-11 07:58:11,436 INFO   ||  [AdminClient clientId=adminclient-1] Node -1 disconnected.   [org.apache.kafka.clients.NetworkClient]
2025-04-11T07:58:11.441465929Z 2025-04-11 07:58:11,437 WARN   ||  [AdminClient clientId=adminclient-1] Connection to node -1 (localhost/127.0.0.1:19092) could not be established. Node may not be available.   [org.apache.kafka.clients.NetworkClient]
2025-04-11T07:58:12.173325179Z 2025-04-11 07:58:12,171 INFO   ||  [AdminClient clientId=adminclient-1] Metadata update failed   [org.apache.kafka.clients.admin.internals.AdminMetadataManager]
2025-04-11T07:58:12.173370346Z org.apache.kafka.common.errors.TimeoutException: Timed out waiting for a node assignment. Call: fetchMetadata
2025-04-11T07:58:12.178183804Z 2025-04-11 07:58:12,177 INFO   ||  App info kafka.admin.client for adminclient-1 unregistered   [org.apache.kafka.common.utils.AppInfoParser]
2025-04-11T07:58:12.178453888Z 2025-04-11 07:58:12,178 INFO   ||  [AdminClient clientId=adminclient-1] Metadata update failed   [org.apache.kafka.clients.admin.internals.AdminMetadataManager]
2025-04-11T07:58:12.178584679Z org.apache.kafka.common.errors.TimeoutException: The AdminClient thread has exited. Call: fetchMetadata
2025-04-11T07:58:12.178751763Z 2025-04-11 07:58:12,178 INFO   ||  [AdminClient clientId=adminclient-1] Timed out 1 remaining operation(s) during close.   [org.apache.kafka.clients.admin.KafkaAdminClient]
2025-04-11T07:58:12.189563929Z 2025-04-11 07:58:12,188 INFO   ||  Metrics scheduler closed   [org.apache.kafka.common.metrics.Metrics]
2025-04-11T07:58:12.189840013Z 2025-04-11 07:58:12,189 INFO   ||  Closing reporter org.apache.kafka.common.metrics.JmxReporter   [org.apache.kafka.common.metrics.Metrics]
2025-04-11T07:58:12.190028638Z 2025-04-11 07:58:12,189 INFO   ||  Metrics reporters closed   [org.apache.kafka.common.metrics.Metrics]
2025-04-11T07:58:12.193162679Z 2025-04-11 07:58:12,190 ERROR  ||  Stopping due to error   [org.apache.kafka.connect.cli.AbstractConnectCli]
2025-04-11T07:58:12.193171388Z org.apache.kafka.connect.errors.ConnectException: Failed to connect to and describe Kafka cluster. Check worker's broker connection and security properties.
2025-04-11T07:58:12.193186513Z 	at org.apache.kafka.connect.runtime.WorkerConfig.lookupKafkaClusterId(WorkerConfig.java:305)
2025-04-11T07:58:12.193189429Z 	at org.apache.kafka.connect.runtime.WorkerConfig.lookupKafkaClusterId(WorkerConfig.java:285)
2025-04-11T07:58:12.193191429Z 	at org.apache.kafka.connect.runtime.WorkerConfig.kafkaClusterId(WorkerConfig.java:415)
2025-04-11T07:58:12.193193929Z 	at org.apache.kafka.connect.cli.AbstractConnectCli.startConnect(AbstractConnectCli.java:124)
2025-04-11T07:58:12.193195846Z 	at org.apache.kafka.connect.cli.AbstractConnectCli.run(AbstractConnectCli.java:94)
2025-04-11T07:58:12.193197888Z 	at org.apache.kafka.connect.cli.ConnectDistributed.main(ConnectDistributed.java:113)
2025-04-11T07:58:12.193199846Z Caused by: java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.TimeoutException: Timed out waiting for a node assignment. Call: listNodes
2025-04-11T07:58:12.193202013Z 	at java.base/java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:396)
2025-04-11T07:58:12.193204013Z 	at java.base/java.util.concurrent.CompletableFuture.get(CompletableFuture.java:2073)
2025-04-11T07:58:12.193205929Z 	at org.apache.kafka.common.internals.KafkaFutureImpl.get(KafkaFutureImpl.java:165)
2025-04-11T07:58:12.193207846Z 	at org.apache.kafka.connect.runtime.WorkerConfig.lookupKafkaClusterId(WorkerConfig.java:299)
2025-04-11T07:58:12.193620013Z 	... 5 more
2025-04-11T07:58:12.193625054Z Caused by: org.apache.kafka.common.errors.TimeoutException: Timed out waiting for a node assignment. Call: listNodes
````

