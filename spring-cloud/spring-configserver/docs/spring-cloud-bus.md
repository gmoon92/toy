# Spring Cloud Bus

Config Server는 설정이 변경되더라도 이를 클라이언트에게 자동으로 푸시하지 않는다.

- Config Server는 설정이 바뀌어도 클라이언트에게 자동으로 알리지 않는다.
- Config Server는 설정 저장소이자 조회 API 역할을 수행한다.
- 따라서 설정 변경 사항은 클라이언트가 다시 조회해야만 반영된다.

이로 인해 설정 변경을 시스템 전체에 반영하기 위한 별도의 "갱신 트리거" 메커니즘이 필요하다.

---

## Spring Cloud Bus 개념

`Spring Cloud Bus`는 서비스 간 이벤트를 전파하기 위한 논리적 `Message Bus`로 이해할 수 있다.

![message-bus](images/message-bus.png)

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
