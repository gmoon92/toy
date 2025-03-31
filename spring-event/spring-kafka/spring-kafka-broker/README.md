# Kafka Admin Client API

Kafka Admin Client API는 Kafka 클러스터를 프로그래밍 방식으로 관리하고 운영할 수 있도록 지원하는 API이다. 
이를 통해 토픽(Topic), 브로커(Broker), ACL(Access Control List) 등의 리소스를 생성, 삭제, 조회 및 수정할 수 있다.

Kafka Admin Client API를 사용하면 토픽 생성 및 삭제 등의 관리 작업을 자동화하거나, 기존 시스템과 통합하여 운영을 최적화할 수 있다.

## 주요 기능

- 토픽 관리: 토픽 생성, 삭제, 조회
- 브로커 관리: 클러스터 내 브로커 정보 조회
- ACL 관리: 접근 제어 리스트(ACL) 설정 및 조회
- 기타 클러스터 설정 변경

## Dependency

아래 Maven 의존성을 추가하여 `Admin Client API`를 사용할 수 있다.

```xml
<dependency>
   <groupId>org.apache.kafka</groupId>
   <artifactId>kafka-clients</artifactId>
   <version>3.4.0</version>
</dependency>
```

## AdminClient를 이용한 토픽 생성 예제

```java
import org.apache.kafka.clients.admin.*;

import java.util.*;

public class KafkaAdminExample {
    public static void main(String[] args) {
        // AdminClient 설정
        Properties config = new Properties();
        config.put("bootstrap.servers", "localhost:9092");

        try (AdminClient client = AdminClient.create(config)) {
            // 새 토픽 생성
            List<NewTopic> topics = Collections.singletonList(new NewTopic("my-topic", 3, (short) 3));
            client.createTopics(topics);
        }
    }
}
```

더 자세한 내용은 [Kafka Admin API Javadoc](https://kafka.apache.org/documentation/#adminapi)을 참고하면 된다.

# 토픽 자동 생성 옵션

``` properties
# broker
auto.create.topics.enable=false

# consumer
allow.auto.create.topics=false
```

프로듀서 토픽에 레코드 발송시 에러 로그 확인.

```text
20:23:56.258 [kafka-producer-network-thread | producer-1] WARN org.apache.kafka.clients.NetworkClient -- [Producer clientId=producer-1] Error while fetching metadata with correlation id 29 : {topic.sample=UNKNOWN_TOPIC_OR_PARTITION}
20:23:57.263 [kafka-producer-network-thread | producer-1] WARN org.apache.kafka.clients.NetworkClient -- [Producer clientId=producer-1] Error while fetching metadata with correlation id 30 : {topic.sample=UNKNOWN_TOPIC_OR_PARTITION}
20:23:58.268 [kafka-producer-network-thread | producer-1] WARN org.apache.kafka.clients.NetworkClient -- [Producer clientId=producer-1] Error while fetching metadata with correlation id 31 : {topic.sample=UNKNOWN_TOPIC_OR_PARTITION}
```

## Reference

- [Apache Kafka - Admin API](https://kafka.apache.org/documentation/#adminapi)
- [confluent - Admin Client API](https://docs.confluent.io/kafka/kafka-apis.html#admin-api)
- [confluent - Kafka AdminClient Configurations for Confluent Platform](https://docs.confluent.io/platform/current/installation/configuration/admin-configs.html)
- [Admin API Java docs](https://kafka.apache.org/40/javadoc/org/apache/kafka/clients/admin/package-summary.html)
