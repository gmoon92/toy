# [Spring Cloud Bus](https://spring.io/projects/spring-cloud-bus)

Config Server는 설정이 변경되더라도 이를 클라이언트에게 자동으로 푸시하지 않는다.

- Config Server는 설정이 바뀌어도 클라이언트에게 자동으로 알리지 않는다.
- Config Server는 설정 저장소이자 조회 API 역할을 수행한다.
- 따라서 설정 변경 사항은 클라이언트가 다시 조회해야만 반영된다.

이로 인해 설정 변경을 시스템 전체에 반영하기 위한 별도의 "갱신 트리거" 메커니즘이 필요하다.

---

## Spring Cloud Bus 개념

`Spring Cloud Bus`는 서비스 간 이벤트를 전파하기 위한 논리적 `Message Bus`로 이해할 수 있다.

![message-bus](./docs/images/message-bus.png)

_[images source - microsoft message queue vs message bus](https://learn.microsoft.com/en-us/previous-versions/msp-n-p/dn589781(v=pandp.10)?redirectedfrom=MSDN)_

`Message Bus`는 하나 이상의 애플리케이션이 다른 하나 이상의 애플리케이션과
**메시지를 비동기적으로 통신할 수 있도록 하는 messaging infrastructure** 를 제공한다.

Message Bus의 일반적인 특성은 다음과 같다.

- Publisher와 Subscriber는 서로를 알지 못한 채로 메시지를 주고받는다.
- 메시지는 Bus에 발행(publish)되며, 필요한 Subscriber가 이를 구독(subscribe)하여 수신한다.
- 기본적으로 Message Bus는 메시지 순서(FIFO)를 보장하지 않는다.

메시지 순서 보장은 구현체에 따라 다르다.

- Kafka: 파티션 단위 FIFO 보장
- RabbitMQ: 설정에 따라 FIFO 보장 가능
- Spring Cloud Bus 관점에서는 메시지 순서에 의존하지 않는 이벤트 전파 모델을 사용한다.

예를 들어 Publisher가 "updated user"와 같은 상태 변경 이벤트를 Message Bus에 발행하면,<br/>
이 이벤트가 필요한 Subscriber는 Bus로부터 해당 이벤트를 수신하여 각자의 컨텍스트에서 처리한다.

---

## 여러 인스턴스 일괄 갱신을 위한 메시지 버스 도입

설정이 변경될 때 오토스케일된 다수의 애플리케이션 인스턴스를 일괄적으로 갱신하기 위해<br/>
Spring Cloud Bus를 활용한 구조를 구성한다.

```text
┌──────────────┐     ┌──────────────┐
│   Client 1   │     │   Client 2   │
│   (Port 8080)│     │   (Port 8081)│
└──────┬───────┘     └──────┬───────┘
       │                    │
       └────────┬───────────┘
                │ Subscribe (Broadcast)
         ┌──────▼───────┐
         │ Message Bus  │
         │ (RabbitMQ/   │
         │  Kafka)      │
         └──────▲───────┘
                │ 2. Publish RefreshEvent
         ┌──────┴───────┐
         │ Config Server│
         │ (Port 8888)  │  1. POST /actuator/busrefresh
         └──────────────┘
```

이 구조를 통해 설정 변경 감지 및 이벤트 전파 책임을 Config Server로 집중시키고,<br/>
클라이언트는 설정을 소비하고 반영하는 역할에만 집중하도록 분리한다.

#### Config Server

- `/actuator/busrefresh` 엔드포인트를 직접 노출한다.
- 외부 트리거(CI/CD, Webhook 등)를 통해 `/actuator/busrefresh`가 호출되면,
    - RefreshEvent를 Message Bus에 발행한다.
- RefreshEvent는 `/actuator/busrefresh`를 호출받은 **Config Server 인스턴스에서 발행된다.**
- `/actuator/busrefresh` 엔드포인트는 반드시 보호되어야 한다.
    - 내부 네트워크에서만 접근
    - 인증 및 권한 제어 필수

#### Message Bus

- RabbitMQ 또는 Kafka 기반으로 구성된다.
- RefreshEvent를 구독 중인 모든 애플리케이션에 이벤트를 브로드캐스트한다.

#### Clients

- Message Bus를 구독한다.
- RefreshEvent 수신 시 Config Server로부터 설정을 다시 조회하고 갱신한다.

---

## `/actuator/refresh` vs `/actuator/busrefresh`

|       | `/actuator/refresh`                                                                                       | `/actuator/busrefresh`                                                                                                                                                                                                                            |
|-------|-----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 영향 범위 | 해당 인스턴스 1개                                                                                                | 동일한 Spring Cloud Bus에 연결된 모든 인스턴스                                                                                                                                                                                                                 |
| 역할    | - 해당 애플리케이션 인스턴스 하나의 설정을 다시 로드한다.<br/>- Config Server로부터 최신 설정을 다시 조회한다.<br/>- @RefreshScope 빈만 다시 초기화된다. | - Message Bus를 통한 이벤트 브로드캐스트 <br/> - 모든 연결된 인스턴스 일괄 갱신                                                                                                                                                                                            |
| 특징    | - 로컬(인스턴스 단위) 동작<br/>- HTTP POST 요청: <br/>- ex) `POST http://app-instance-1:8080/actuator/refresh`        | **동작:**<br/>1. Config Server에 `POST /actuator/busrefresh` 호출<br/> 2. RefreshEvent를 Message Bus에 발행<br/> 3. 모든 클라이언트가 자동으로 설정 갱신 <br/><br/> **필터링:**<br/>- 특정 서비스만: `/actuator/busrefresh/{destination}`<br/> - 설정: `spring.cloud.bus.destination` |
| 주의사항  | - 해당 인스턴스만 갱신된다.<br/> - 오토스케일 환경에서는 모든 인스턴스를 개별 호출해야 한다.<br/> - 인스턴스 수가 많아질수록 운영 부담이 커진다.<br/>            | - `/actuator/busrefresh` 엔드포인트는 반드시 보호되어야 한다. <br/> - 내부 네트워크에서만 접근<br/> - 인증 및 권한 제어 필수                                                                                                                                                          |

---

## Dependencies

Config Server 역시 Spring Cloud Bus의 한 구성원으로 동작하며,<br/>
**RefreshEvent를 발행하는 중앙 트리거 역할을 수행한다.**

```kotlin
// bus-amqp 대신 bus-kafka 사용 가능
//implementation("org.springframework.cloud:spring-cloud-starter-bus-kafka")

// Config Server
implementation("org.springframework.cloud:spring-cloud-starter-bus-amqp")

// Config Client
implementation("org.springframework.cloud:spring-cloud-starter-bus-amqp")
```

---

## Bus Endpoints

Spring Cloud Bus는 세 가지 엔드포인트를 제공한다.

- `/actuator/busrefresh`
- `/actuator/busshutdown`
- `/actuator/busenv`

이들은 각각 Spring Cloud Commons의 개별 actuator 엔드포인트인 `/actuator/refresh`, `/actuator/shutdown`, `/actuator/env`에 대응된다.

### Bus Refresh Endpoint

`/actuator/busrefresh` 엔드포인트는 RefreshScope 캐시를 지우고 @ConfigurationProperties를 다시 바인딩한다.

이 엔드포인트를 노출하려면 다음 설정을 추가해야 한다.

```properties
management.endpoints.web.exposure.include=busrefresh
```

> 설정이 변경되었을 때 모든 서비스 인스턴스의 설정을 한 번에 갱신하고 싶을 때 사용한다. <br/>
> 예를 들어 데이터베이스 URL이나 외부 API 키가 변경되었다면, Config Server에 `POST /actuator/busrefresh`를 한 번만 호출하면 모든 클라이언트가 자동으로 새 설정을 적용한다.

### Bus Env Endpoint

`/actuator/busenv` 엔드포인트는 여러 인스턴스에 걸쳐 지정된 key/value 쌍으로 각 인스턴스의 환경 변수를 업데이트한다.

이 엔드포인트를 노출하려면 다음 설정을 추가해야 한다.

```properties
management.endpoints.web.exposure.include=busenv
```

`/actuator/busenv` 엔드포인트는 다음 형식의 POST 요청을 받는다.

```json
{
  "name": "key1",
  "value": "value1"
}
```

> 애플리케이션 재시작 없이 환경 변수를 동적으로 변경할 수 있다.<br/>
> 예를 들어 로그 레벨을 DEBUG로 변경하고 싶다면, `{"name": "logging.level.root", "value": "DEBUG"}`를 전송하면 모든 인스턴스의 로그 레벨이 변경된다.

### Bus Shutdown Endpoint

`/actuator/busshutdown` 엔드포인트는 애플리케이션을 우아하게(gracefully) 종료한다.

이 엔드포인트를 노출하려면 다음 설정을 추가해야 한다.

```properties
management.endpoints.web.exposure.include=busshutdown
```

busshutdown 엔드포인트에 POST 요청을 보내서 사용할 수 있다.

특정 애플리케이션을 타겟팅하려면 `/busshutdown`에 POST 요청을 보내고 선택적으로 bus id를 지정할 수 있다.

```bash
$ curl -X POST http://localhost:8080/actuator/busshutdown
```

bus id를 지정하여 특정 애플리케이션 인스턴스를 타겟팅할 수도 있다.

```bash
$ curl -X POST http://localhost:8080/actuator/busshutdown/busid:123
```

> 모든 서비스 인스턴스를 한 번에 종료할 수 있다. 주로 유지보수나 긴급 상황에서 사용한다. <br/>
> 운영 환경에서는 보안상 이 엔드포인트를 비활성화하거나 매우 강력한 인증을 걸어두어야 한다.

---

## 인스턴스 지정하기 (Addressing Instances)

### 개별 인스턴스 지정

애플리케이션의 각 인스턴스는 service ID를 가지며, 이 값은 `spring.cloud.bus.id`로 설정할 수 있다.
이 값은 콜론으로 구분된 식별자 목록으로, 덜 구체적인 것부터 더 구체적인 순서로 구성된다.

기본값은 환경으로부터 `spring.application.name`과 `server.port`(또는 설정된 경우 `spring.application.index`)의 조합으로 구성된다.

ID의 기본값은 `app:index:id` 형식으로 구성된다.

- **app**: `vcap.application.name`이 있으면 그 값을, 없으면 `spring.application.name`
- **index**: `vcap.application.instance_index`, `spring.application.index`, `local.server.port`, `server.port` 순서로 찾거나 0
- **id**: `vcap.application.instance_id`가 있으면 그 값을, 없으면 랜덤 값

HTTP 엔드포인트는 "destination" 경로 파라미터를 받을 수 있다.
예: `/busrefresh/customers:9000`에서 destination은 service ID다.
버스에서 해당 ID를 가진 인스턴스만 메시지를 처리하고, 다른 모든 인스턴스는 무시한다.

> 예를 들어 `customers` 서비스가 3개의 인스턴스(8080, 8081, 8082 포트)로 실행 중일 때,
`/busrefresh/customers:9000`을 호출하면 customers:9000 인스턴스만 설정을 갱신한다.<br/>
> 이는 특정 인스턴스에만 문제가 있을 때 해당 인스턴스만 선택적으로 갱신하고 싶을 때 유용하다.

### 서비스의 모든 인스턴스 지정

"destination" 파라미터는 Spring PathMatcher를 사용하여(경로 구분자는 콜론 `:`) 인스턴스가 메시지를 처리할지 결정한다.

예를 들어 `/busenv/customers:**`는 service ID의 나머지 부분에 관계없이 "customers" 서비스의 모든 인스턴스를 대상으로 한다.

> `/busrefresh/customers:**` 를 호출하면 customers 서비스의 모든 인스턴스(8080, 8081, 8082 등)가 한 번에 설정을 갱신한다.
`**`는 와일드카드로, "customers로 시작하는 모든 서비스 ID"를 의미한다.<br/>
> 실무에서는 특정 서비스의 모든 인스턴스를 갱신할 때 이 방식을 가장 많이 사용한다.

### Service ID는 반드시 고유해야 함

버스는 이벤트 중복 처리를 방지하기 위해 두 번 체크한다.

- 한 번은 원본 ApplicationEvent에서
- 한 번은 큐에서

이를 위해 발신 service ID와 현재 service ID를 비교한다.
만약 서비스의 여러 인스턴스가 동일한 ID를 가지면 이벤트가 처리되지 않는다.

로컬 머신에서 실행할 때는 각 서비스가 다른 포트에 있으며, 그 포트가 ID의 일부가 된다.
Cloud Foundry는 구분을 위해 인덱스를 제공한다.
Cloud Foundry 외부에서 ID가 고유하도록 보장하려면, 각 서비스 인스턴스마다 `spring.application.index`를 고유한 값으로 설정해야 한다.

> 같은 서비스를 여러 개 띄울 때 각 인스턴스가 서로 다른 ID를 가져야 Bus 이벤트가 제대로 동작한다.<br/>
> 로컬에서 테스트할 때는 포트가 다르면 자동으로 다른 ID를 갖지만,
> 도커 컨테이너나 쿠버네티스처럼 포트가 같은 환경에서는 명시적으로 `spring.application.index`를 설정해야 한다.<br/>
> 예: `spring.application.index=1`, `spring.application.index=2` 등으로 각 인스턴스를 구분한다.

---

## 설정 (Configuration)

### 메시지 브로커 커스터마이징

Spring Cloud Bus는 Spring Cloud Stream을 사용하여 메시지를 브로드캐스트한다.
따라서 메시지가 흐르도록 하려면 선택한 바인더 구현체를 클래스패스에 포함하기만 하면 된다.

AMQP(RabbitMQ) 및 Kafka용 편리한 스타터가 있다: `spring-cloud-starter-bus-[amqp|kafka]`

일반적으로 Spring Cloud Stream은 미들웨어 설정을 위해 Spring Boot 자동 설정 규칙을 따른다.
예를 들어 AMQP 브로커 주소는 `spring.rabbitmq.*` 설정 속성으로 변경할 수 있다.

Spring Cloud Bus는 `spring.cloud.bus.*`에 몇 가지 네이티브 설정 속성이 있다.
예를 들어 `spring.cloud.bus.destination`은 외부 미들웨어로 사용할 토픽의 이름이다.
일반적으로 기본값으로 충분하다.

메시지 브로커 설정을 커스터마이징하는 방법에 대한 자세한 내용은 Spring Cloud Stream 문서를 참조하라.

> Spring Cloud Bus는 내부적으로 RabbitMQ나 Kafka 같은 메시지 브로커를 사용한다.<br/>
> 설정은 매우 간단한데, `spring-cloud-starter-bus-amqp` 의존성을 추가하면 Spring Boot가 자동으로 RabbitMQ 연결을 설정해준다.<br/>
> 만약 RabbitMQ 서버가 localhost가 아닌 다른 곳에 있다면 `application.yml`에 다음과 같이 설정한다:

```yaml
spring:
  rabbitmq:
    host: my-rabbitmq-server.com
    port: 5672
    username: user
    password: pass
```

### Bus 이벤트 추적 (Tracing Bus Events)

Bus 이벤트(RemoteApplicationEvent의 하위 클래스)는 `spring.cloud.bus.trace.enabled=true`로 설정하여 추적할 수 있다.

이렇게 하면 Spring Boot TraceRepository(존재하는 경우)가 전송된 각 이벤트와 각 서비스 인스턴스의 모든 확인 응답(ack)을 보여준다.

다음 예제는 `/trace` 엔드
포인트에서 가져온 것이다:

```json
[
  {
    "timestamp": "2015-11-26T10:24:44.411+0000",
    "info": {
      "signal": "spring.cloud.bus.ack",
      "type": "RefreshRemoteApplicationEvent",
      "id": "c4d374b7-58ea-4928-a312-31984def293b",
      "origin": "stores:8081",
      "destination": "*:**"
    }
  },
  {
    "timestamp": "2015-11-26T10:24:41.864+0000",
    "info": {
      "signal": "spring.cloud.bus.sent",
      "type": "RefreshRemoteApplicationEvent",
      "id": "c4d374b7-58ea-4928-a312-31984def293b",
      "origin": "customers:9000",
      "destination": "*:**"
    }
  },
  {
    "timestamp": "2015-11-26T10:24:41.862+0000",
    "info": {
      "signal": "spring.cloud.bus.ack",
      "type": "RefreshRemoteApplicationEvent",
      "id": "c4d374b7-58ea-4928-a312-31984def293b",
      "origin": "customers:9000",
      "destination": "*:**"
    }
  }
]
```

위 추적은 RefreshRemoteApplicationEvent가 customers:9000에서 전송되어 모든 서비스에 브로드캐스트되었고,
customers:9000과 stores:8081에서 수신(ack)되었음을 보여준다.

확인 신호(ack signal)를 직접 처리하려면 앱에 AckRemoteApplicationEvent 및 SentApplicationEvent 타입에 대한 @EventListener를 추가하면 된다(추적도 활성화해야
함).
또는 TraceRepository에 접근하여 거기서 데이터를 가져올 수도 있다.

모든 Bus 애플리케이션은 확인 응답을 추적할 수 있다.
그러나 때로는 데이터에 대해 더 복잡한 쿼리를 수행하거나 전문 추적 서비스로 전달할 수 있는 중앙 서비스에서 이 작업을 수행하는 것이 유용하다.

> Bus 이벤트 추적은 디버깅할 때 매우 유용하다.<br/>
> "내가 보낸 설정 갱신 이벤트가 정말로 모든 인스턴스에 도달했을까?"를 확인할 수 있다.<br/>
> 추적을 활성화하면 누가 이벤트를 보냈는지(`sent`), 누가 받았는지(`ack`)를 시간 순서대로 볼 수 있다.<br/>
> 운영 환경에서는 보통 비활성화하지만, 개발이나 문제 해결 시에는 활성화해서 사용한다.

---

## 커스텀 이벤트 (Custom Events)

### 자체 이벤트 브로드캐스팅

Bus는 `RemoteApplicationEvent` 타입의 모든 이벤트를 전달할 수 있다.
기본 전송 방식은 JSON이며, 역직렬화기는 어떤 타입이 사용될지 미리 알아야 한다.

새로운 타입을 등록하려면 `org.springframework.cloud.bus.event`의 하위 패키지에 넣어야 한다.

이벤트 이름을 커스터마이징하려면 커스텀 클래스에 `@JsonTypeName`을 사용하거나,
클래스의 단순 이름을 사용하는 기본 전략에 의존할 수 있다.

**중요:** 프로듀서와 컨슈머 모두 클래스 정의에 접근할 수 있어야 한다.

> Spring Cloud Bus는 설정 갱신(`RefreshRemoteApplicationEvent`) 외에도 커스텀 이벤트를 정의해서 서비스 간 통신에 사용할 수 있다.<br/>
> 예를 들어 "사용자가 로그인했습니다"라는 이벤트를 정의하고,<br/>
> 모든 서비스 인스턴스가 이를 받아서 각자의 캐시를 업데이트하는 식으로 활용할 수 있다.

### 커스텀 패키지에 이벤트 등록

커스텀 이벤트에 `org.springframework.cloud.bus.event`의 하위 패키지를 사용할 수 없거나 원하지 않는 경우,
`@RemoteApplicationEventScan` 어노테이션을 사용하여 RemoteApplicationEvent 타입의 이벤트를 스캔할 패키지를 지정해야 한다.

`@RemoteApplicationEventScan`으로 지정된 패키지에는 하위 패키지도 포함된다.

예를 들어 다음과 같은 커스텀 이벤트 MyEvent를 고려해보자:

```java
public class MyEvent extends RemoteApplicationEvent {
	//...
}
```

다음과 같은 방법으로 역직렬화기에 해당 이벤트를 등록할 수 있다:

```java

@Configuration
@RemoteApplicationEventScan
public class BusConfiguration {
	//...
}
```

값을 지정하지 않으면 `@RemoteApplicationEventScan`이 사용된 클래스의 패키지가 등록된다.
이 예제에서는 BusConfiguration의 패키지인 com.acme가 등록된다.

또한 `@RemoteApplicationEventScan`의 value, basePackages 또는 basePackageClasses 속성을 사용하여
명시적으로 스캔할 패키지를 지정할 수도 있다:

```java

@Configuration
//@RemoteApplicationEventScan({"com.acme", "foo.bar"})
//@RemoteApplicationEventScan(basePackages = {"com.acme", "foo.bar", "fizz.buzz"})
@RemoteApplicationEventScan(basePackageClasses = BusConfiguration.class)
public class BusConfiguration {
	//...
}
```

위의 모든 `@RemoteApplicationEventScan` 예제는 동일하며,
`@RemoteApplicationEventScan`에서 패키지를 명시적으로 지정하여 com.acme 패키지가 등록된다.

여러 베이스 패키지를 지정하여 스캔할 수 있다.

> 커스텀 이벤트를 만들 때는 보통 자신의 프로젝트 패키지 안에 만든다(예: com.mycompany.events).<br/>
> 이 경우 Spring Cloud Bus에게 "이 패키지도 스캔해서 이벤트를 찾아줘"라고 알려줘야 한다.<br/>
> `@RemoteApplicationEventScan`이 바로 그 역할을 한다.<br/>
> 이 어노테이션을 설정 클래스에 붙이면 지정한 패키지의 모든 커스텀 이벤트를 자동으로 인식한다.

---

## Reference

- spring.io
    - [Spring Cloud Bus](https://docs.spring.io/spring-cloud-bus/reference/spring-cloud-bus.html)
    - [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream)
