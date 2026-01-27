# [Spring Cloud Bus](https://spring.io/projects/spring-cloud-bus)

Config Server는 설정이 변경되더라도 이를 클라이언트에게 자동으로 푸시하지 않는다.

- Config Server는 설정이 바뀌어도 클라이언트에게 자동으로 알리지 않는다.
- Config Server는 설정 저장소이자 조회 API 역할을 수행한다.
- 따라서 설정 변경 사항은 클라이언트가 다시 조회해야만 반영된다.

이로 인해 설정 변경을 시스템 전체에 반영하기 위한 별도의 "갱신 트리거" 메커니즘이 필요하다.

---

## 🚀 Quick Start

```bash
# 1. RabbitMQ 실행 (Docker)
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 2. Config Server 실행 (Port 8888)
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-server:bootRun

# 3. Config Client 인스턴스 여러 개 실행
# Terminal 1:
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun --args='--server.port=8080'

# Terminal 2:
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun --args='--server.port=8081'

# Terminal 3:
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun --args='--server.port=8082'

# 4. 현재 설정 확인 (모든 인스턴스)
curl http://localhost:8080/config
curl http://localhost:8081/config
curl http://localhost:8082/config

# 5. 설정 파일 수정 (재시작 없이 즉시 반영 가능)
# Config Server의 설정 파일 경로 (Native 파일 시스템):
# spring-cloud-bus-server/src/main/resources/config-repo/{label}/{application-name}.yml
# 예시: config-repo/alpha/client.yml, config-repo/alpha/client-dev.yml
# Git 커밋 없이 파일 수정만으로 바로 테스트 가능!

# 6. Config Server를 통해 모든 인스턴스 일괄 갱신 (단일 호출!)
curl -X POST http://localhost:8888/actuator/busrefresh

# 7. 변경된 설정 확인 (모든 인스턴스가 자동으로 갱신됨)
curl http://localhost:8080/config
curl http://localhost:8081/config
curl http://localhost:8082/config

# 8. 커스텀 이벤트 테스트 (사용자 로그인 이벤트)
curl -X POST http://localhost:8888/events/logins \
  -H "Content-Type: application/json" \
  -d '{"username": "john.doe", "ipAddress": "192.168.1.100"}'

# 9. 모든 인스턴스에서 로그인 이력 확인 (이벤트가 모든 인스턴스에 전파됨)
curl http://localhost:8080/events/logins
curl http://localhost:8081/events/logins
curl http://localhost:8082/events/logins
```

---

## 목차

1. [Spring Cloud Bus 개념](#spring-cloud-bus-개념)
2. [여러 인스턴스 일괄 갱신을 위한 메시지 버스 도입](#여러-인스턴스-일괄-갱신을-위한-메시지-버스-도입)
3. [아키텍처 및 컴포넌트](#아키텍처-및-컴포넌트)
4. [/actuator/refresh vs /actuator/busrefresh](#actuatorrefresh-vs-actuatorbusrefresh)
5. [Dependencies](#dependencies)
6. [Bus Endpoints](#bus-endpoints)
7. [설정 갱신 메커니즘](#설정-갱신-메커니즘)
8. [인스턴스 지정하기 (Addressing Instances)](#인스턴스-지정하기-addressing-instances)
9. [설정 (Configuration)](#설정-configuration)
10. [커스텀 이벤트 (Custom Events)](#커스텀-이벤트-custom-events)
11. [실전 시나리오](#실전-시나리오)
12. [Bus Refresh 이벤트 동작 원리](#bus-refresh-이벤트-동작-원리)
13. [모니터링 및 추적](#모니터링-및-추적)
14. [베스트 프랙티스](#베스트-프랙티스)
15. [Reference](#reference)

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

---

## 아키텍처 및 컴포넌트

### Config Server

Message Bus(RabbitMQ/Kafka)를 통해 연결된 모든 클라이언트에 설정 변경사항을 브로드캐스팅한다.

**주요 기능:**

- `/actuator/busrefresh` 엔드포인트로 모든 인스턴스에 갱신 이벤트 트리거
- `/actuator/busenv` 엔드포인트로 환경 변수 동적 업데이트
- Native 파일 시스템 백엔드를 사용한 중앙화된 설정 관리 (로컬 개발 환경)
    - Git 커밋 없이 파일 수정만으로 즉시 테스트 가능
    - 설정 파일 위치: `config-repo/{label}/{application-name}.yml`
- RefreshEvent를 Message Bus에 발행하는 중앙 트리거 역할 수행

**동작 흐름:**

```
외부 트리거 (CI/CD, Webhook)
        │
        ▼
POST /actuator/busrefresh
        │
        ▼
Config Server가 Message Bus에 RefreshEvent 발행
        │
        ▼
Message Bus (RabbitMQ)가 모든 클라이언트에 브로드캐스트
        │
        ▼
모든 클라이언트가 이벤트를 수신하고 설정을 갱신
```

**보안 고려사항:**

- `/actuator/busrefresh` 엔드포인트는 반드시 보호되어야 한다
- 내부 네트워크에서만 접근
- 인증 및 권한 제어 필수

### Message Bus

- RabbitMQ 또는 Kafka 기반으로 구성된다.
- RefreshEvent를 구독 중인 모든 애플리케이션에 이벤트를 브로드캐스트한다.

### Clients

Spring Cloud Bus 메시지 브로커를 구독하여 Config Server로부터 설정 갱신 이벤트를 수신한다.

**주요 기능:**

- Message Bus(RabbitMQ)를 구독하여 RefreshRemoteApplicationEvent 수신
- 갱신 이벤트 수신 시 `@RefreshScope` 빈을 자동으로 갱신
- Config Server에 연결하여 중앙화된 설정 조회
- 애플리케이션 재시작 없이 동적 설정 업데이트 지원
- 커스텀 버스 이벤트 수신 (예: UserLoginEvent)

**설정 갱신 흐름:**

```
1. Config Server가 POST /actuator/busrefresh 요청 수신
   ↓
2. Config Server가 Message Bus에 RefreshRemoteApplicationEvent 발행
   ↓
3. 이 클라이언트가 RabbitMQ로부터 이벤트 수신
   ↓
4. Spring Cloud Bus가 @RefreshScope 빈 재생성 트리거
   ↓
5. 클라이언트가 Config Server로부터 최신 설정 조회
   ↓
6. 애플리케이션이 재시작 없이 새로운 설정값 사용
```

**Refresh Scope:**

- `@RefreshScope`로 어노테이션된 빈은 갱신 이벤트 발생 시 재생성
- Config Server의 새로운 설정값 적용

---

## /actuator/refresh vs /actuator/busrefresh

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

## 설정 갱신 메커니즘

Spring Cloud에서 설정 값에 접근하는 세 가지 방식과 각각의 갱신 메커니즘을 이해하는 것이 중요하다.

- https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/application-context-services.html#environment-changes

### 1. @Value (eager binding - RefreshScope 필요)

```java

@RefreshScope  // ← 없으면 갱신 안 됨!
@Component
public class MyService {
	@Value("${app.message}")
	private String message;  // 빈 생성 시 주입되고 고정됨
}
```

**동작 방식:**

- 빈 생성 시점에 `Environment.getProperty()`로 값을 조회하여 필드에 저장
- 이후 필드 값만 참조 (Environment를 다시 조회하지 않음)
- → **RefreshScope로 빈 재생성 필요**

**갱신 메커니즘:**

```
refresh 호출
    ↓
RefreshScope 캐시 클리어
    ↓
다음 빈 접근 시
    ↓
새 빈 생성 + 최신 값 재주입
```

### 2. Environment (lazy lookup - RefreshScope 불필요)

```java

@Component  // ← RefreshScope 불필요!
public class MyService {
	private final Environment environment;

	public String getMessage() {
		return environment.getProperty("app.message");  // 호출 시점에 조회
	}
}
```

**동작 방식:**

- 호출 시마다 `PropertySource` 실시간 조회
- PropertySource가 교체되면 자동 반영
- → **RefreshScope 불필요**
- → **refresh 전에도 최신 값 반환**

**갱신 메커니즘:**

```
refresh 호출
    ↓
PropertySource 교체
    ↓
environment.getProperty() 호출
    ↓
새로운 PropertySource에서 즉시 최신 값 반환
```

### 3. @ConfigurationProperties (rebinding - RefreshScope 불필요)

```java

@ConfigurationProperties(prefix = "app")  // ← RefreshScope 불필요!
public class AppProperties {
	private String message;  // 일반 필드 (프록시 아님!)
}
```

**동작 방식:**

- 일반 POJO (프록시 없음)
- [
  `ConfigurationPropertiesRebinder`](https://github.com/spring-cloud/spring-cloud-commons/blob/main/spring-cloud-context/src/main/java/org/springframework/cloud/context/properties/ConfigurationPropertiesRebinder.java)
  가 자동으로 재바인딩
- → **RefreshScope 불필요**

**갱신 메커니즘:**

```
refresh 호출
    ↓
EnvironmentChangeEvent 발행
    ↓
ConfigurationPropertiesRebinder가 이벤트 감지
    ↓
Environment에서 최신 값 읽어서 필드 재설정 (리플렉션)
```

### 비교표

| 방식                         | 바인딩 시점         | RefreshScope 필요 | 갱신 메커니즘       | refresh 전 최신 값 |
|----------------------------|----------------|-----------------|---------------|----------------|
| `@Value`                   | 빈 생성 시 (eager) | ✅ 필수            | 빈 재생성         | ❌              |
| `Environment`              | 호출 시 (lazy)    | ❌ 불필요           | 실시간 조회        | ✅              |
| `@ConfigurationProperties` | 초기 + Rebind 시  | ❌ 불필요           | Rebinder 재바인딩 | ❌              |

### 권장 사용 방식

#### 일반적인 설정: @ConfigurationProperties

```java

@ConfigurationProperties(prefix = "app")
public class AppProperties {
	private String message;
	private Database database;
	// getter/setter
}
```

**장점:**

- ✅ RefreshScope 불필요
- ✅ 타입 안전
- ✅ IDE 자동완성
- ✅ 구조화된 설정
- ✅ 자동 갱신

#### 동적 조회가 필요한 경우: Environment

```java

@Component
public class DynamicConfigService {
	private final Environment environment;

	public String getValue(String key) {
		return environment.getProperty(key);  // 동적 키
	}
}
```

**장점:**

- ✅ RefreshScope 불필요
- ✅ refresh 전에도 최신 값
- ✅ 동적 키 지원

#### 간단한 값 주입: @Value + @RefreshScope (가급적 피하기)

```java

@RefreshScope
@Component
public class SimpleService {
	@Value("${app.flag:false}")
	private boolean featureFlag;
}
```

**권장하지 않는 이유:**

- RefreshScope 필요
- 타입 안전성 없음
- 구조화 어려움

### @RefreshScope를 사용하지 말아야 할 경우

- **@ConfigurationProperties 빈** - 자동으로 갱신됨
- **복잡한 상태를 가진 싱글톤 빈** - 갱신 시 상태가 손실됨
- **초기화 비용이 큰 빈** - 잦은 갱신이 성능에 영향을 줌

> 상세한 동작 원리는 [refresh-scope-mechanisms.md](./docs/refresh-scope-mechanisms.md) 문서를 참고하세요.

---

## 인스턴스 지정하기 (Addressing Instances)

### Service ID 구조

애플리케이션의 각 인스턴스는 service ID를 가지며, 이 값은 `spring.cloud.bus.id`로 설정할 수 있다.
이 값은 콜론으로 구분된 식별자 목록으로, 덜 구체적인 것부터 더 구체적인 순서로 구성된다.

**서비스 ID 형식:** `app:port:randomId`

- **app**: `spring.application.name`
- **port**: `server.port`
- **randomId**: 고유성을 보장하기 위한 랜덤 값

ID의 기본값은 `app:index:id` 형식으로 구성된다.

- **app**: `vcap.application.name`이 있으면 그 값을, 없으면 `spring.application.name`
- **index**: `vcap.application.instance_index`, `spring.application.index`, `local.server.port`, `server.port` 순서로 찾거나 0
- **id**: `vcap.application.instance_id`가 있으면 그 값을, 없으면 랜덤 값

### 개별 인스턴스 지정

HTTP 엔드포인트는 "destination" 경로 파라미터를 받을 수 있다.
예: `/busrefresh/customers:9000`에서 destination은 service ID다.
버스에서 해당 ID를 가진 인스턴스만 메시지를 처리하고, 다른 모든 인스턴스는 무시한다.

> 예를 들어 `customers` 서비스가 3개의 인스턴스(8080, 8081, 8082 포트)로 실행 중일 때,
> `/busrefresh/customers:9000`을 호출하면 customers:9000 인스턴스만 설정을 갱신한다.<br/>
> 이는 특정 인스턴스에만 문제가 있을 때 해당 인스턴스만 선택적으로 갱신하고 싶을 때 유용하다.

### 서비스의 모든 인스턴스 지정

"destination" 파라미터는 Spring PathMatcher를 사용하여(경로 구분자는 콜론 `:`) 인스턴스가 메시지를 처리할지 결정한다.

예를 들어 `/busenv/customers:**`는 service ID의 나머지 부분에 관계없이 "customers" 서비스의 모든 인스턴스를 대상으로 한다.

> `/busrefresh/customers:**` 를 호출하면 customers 서비스의 모든 인스턴스(8080, 8081, 8082 등)가 한 번에 설정을 갱신한다.
> `**`는 와일드카드로, "customers로 시작하는 모든 서비스 ID"를 의미한다.<br/>
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

### 커스텀 이벤트 생성

**1. RemoteApplicationEvent 상속**

```java

@JsonTypeName("UserLoginEvent")
@NoArgsConstructor
@Getter
@Setter
public class UserLoginEvent extends RemoteApplicationEvent {
	private String username;
	private String ipAddress;

	public UserLoginEvent(
	  Object source,
	  String originService,
	  String destinationService,
	  String username,
	  String ipAddress
	) {
		super(source, originService, destinationService);
		this.username = username;
		this.ipAddress = ipAddress;
	}
}
```

**필수 요구사항:**

- `RemoteApplicationEvent` 확장
- `@JsonTypeName` 어노테이션 추가 (타입명 명시)
- no-arg 생성자 제공 (JSON 역직렬화용)
- Producer와 Consumer 모두 동일한 클래스 정의 필요

### 커스텀 패키지에 이벤트 등록

커스텀 이벤트에 `org.springframework.cloud.bus.event`의 하위 패키지를 사용할 수 없거나 원하지 않는 경우,
`@RemoteApplicationEventScan` 어노테이션을 사용하여 RemoteApplicationEvent 타입의 이벤트를 스캔할 패키지를 지정해야 한다.

`@RemoteApplicationEventScan`으로 지정된 패키지에는 하위 패키지도 포함된다.

**스캔 옵션:**

- 인자 없음: 설정 클래스의 패키지와 모든 하위 패키지 스캔
- `basePackages`: 명시적으로 스캔할 패키지 지정
- `basePackageClasses`: 마커 클래스를 사용하여 스캔 패키지 정의

**예시:**

```java

@Configuration
@RemoteApplicationEventScan(basePackages = "com.gmoon.springcloudbus.common.events")
public class BusConfiguration {
}
```

또는

```java

@Configuration
@RemoteApplicationEventScan  // 이 클래스의 패키지 스캔
public class BusConfiguration {
}
```

여러 베이스 패키지를 지정하여 스캔할 수 있다.

```java

@Configuration
@RemoteApplicationEventScan(basePackages = {"com.acme.events", "com.foo.events"})
public class BusConfiguration {
}
```

> 커스텀 이벤트를 만들 때는 보통 자신의 프로젝트 패키지 안에 만든다(예: com.mycompany.events).<br/>
> 이 경우 Spring Cloud Bus에게 "이 패키지도 스캔해서 이벤트를 찾아줘"라고 알려줘야 한다.<br/>
> `@RemoteApplicationEventScan`이 바로 그 역할을 한다.<br/>
> 이 어노테이션을 설정 클래스에 붙이면 지정한 패키지의 모든 커스텀 이벤트를 자동으로 인식한다.

### 커스텀 이벤트 발행 및 수신

**이벤트 발행 (Server 또는 Client):**

```java

@RestController
public class EventPublisher {
	private final ApplicationEventPublisher eventPublisher;

	@Value("${spring.cloud.bus.id}")
	private String busId;

	@PostMapping("/api/events/user-login")
	public void publishEvent(@RequestBody Map<String, String> payload) {
		UserLoginEvent event = new UserLoginEvent(
		  this,
		  busId,           // Origin: 이 인스턴스
		  "**:**",         // Destination: 모든 인스턴스
		  payload.get("username"),
		  payload.get("ipAddress")
		);

		eventPublisher.publishEvent(event);
	}
}
```

**이벤트 수신 (모든 인스턴스):**

```java

@Component
public class CustomEventListener {
	@EventListener
	public void onUserLoginEvent(UserLoginEvent event) {
		log.info("User {} logged in from {}",
		  event.getUsername(),
		  event.getIpAddress());

		// 캐시 무효화, 세션 관리 등
		clearUserCache(event.getUsername());
	}
}
```

**이벤트 흐름:**

```
1. POST /api/events/user-login (어떤 인스턴스든)
   ↓
2. 컨트롤러가 UserLoginEvent 발행
   ↓
3. Spring Cloud Bus가 이벤트를 JSON으로 직렬화
   ↓
4. 이벤트가 RabbitMQ 토픽에 발행됨
   ↓
5. 구독된 모든 인스턴스가 이벤트를 수신하고 역직렬화
   ↓
6. 각 인스턴스의 @EventListener가 이벤트를 처리
```

### 커스텀 이벤트 사용 사례

**캐시 무효화:**

```java

@EventListener
public void onUserLoginEvent(UserLoginEvent event) {
	cacheManager.getCache("userCache").evict(event.getUsername());
}
```

**세션 관리:**

```java

@EventListener
public void onUserLoginEvent(UserLoginEvent event) {
	sessionRegistry.registerSession(
	  event.getUsername(),
	  event.getOriginService()
	);
}
```

**보안 감사:**

```java

@EventListener
public void onUserLoginEvent(UserLoginEvent event) {
	auditService.log(
	  "User login",
	  event.getUsername(),
	  event.getIpAddress(),
	  event.getTimestamp()
	);
}
```

**분석 및 모니터링:**

```java

@EventListener
public void onUserLoginEvent(UserLoginEvent event) {
	meterRegistry.counter("user.logins",
	  "username", event.getUsername()
	).increment();
}
```

---

## 실전 시나리오

### 시나리오 1: 설정 변경 일괄 반영

**상황:** 데이터베이스 연결 URL이 변경되어 모든 서비스 인스턴스에 반영해야 함

**절차:**

```bash
# 1. RabbitMQ 실행 (Docker)
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 2. Config Server 실행 (port 8888)
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-server:bootRun

# 3. Client 인스턴스 여러 개 실행
# Terminal 1:
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun --args='--server.port=8080'

# Terminal 2:
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun --args='--server.port=8081'

# Terminal 3:
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun --args='--server.port=8082'

# 4. 현재 설정 확인
curl http://localhost:8080/api/config
curl http://localhost:8081/api/config
curl http://localhost:8082/api/config

# 5. Config Server의 설정 파일 수정 (Git 커밋 불필요!)
# spring-cloud-bus-server/src/main/resources/config-repo/alpha/client.yml 파일 수정
# 예시:
# app:
#   message: "Updated via Bus Refresh!"
#   version: "alpha 2.0.0"
#   feature:
#     enabled: true

# 6. Config Server를 통해 모든 인스턴스 갱신 트리거 (단일 호출!)
curl -X POST http://localhost:8888/actuator/busrefresh

# 7. 모든 인스턴스가 업데이트된 설정 확인
curl http://localhost:8080/config  # Updated!
curl http://localhost:8081/config  # Updated!
curl http://localhost:8082/config  # Updated!
```

### 시나리오 2: 커스텀 이벤트를 통한 분산 캐시 동기화

**상황:** 사용자 로그인 시 모든 인스턴스의 캐시를 무효화해야 함

**절차:**

```bash
# 1. 여러 클라이언트 인스턴스 실행 (위 시나리오 1의 1-3 단계 동일)

# 2. Config Server에서 커스텀 이벤트 발행
curl -X POST http://localhost:8888/api/events/user-login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "ipAddress": "192.168.1.100"
  }'

# 3. 모든 인스턴스의 로그에서 이벤트 수신 확인
# Instance 8080 로그: "User john.doe logged in from 192.168.1.100"
# Instance 8081 로그: "User john.doe logged in from 192.168.1.100"
# Instance 8082 로그: "User john.doe logged in from 192.168.1.100"

# 4. 로그인 이력 확인 (모든 인스턴스가 동일한 데이터 보유)
curl http://localhost:8080/api/login-history
curl http://localhost:8081/api/login-history
curl http://localhost:8082/api/login-history
```

### 시나리오 3: 특정 서비스만 선택적 갱신

**상황:** 여러 서비스가 있고, customers 서비스만 갱신해야 함

```bash
# customers 서비스의 모든 인스턴스만 갱신
curl -X POST http://localhost:8888/actuator/busrefresh/customers:**

# customers 서비스의 특정 인스턴스만 갱신
curl -X POST http://localhost:8888/actuator/busrefresh/customers:8080:abc123
```

---

## Bus Refresh 이벤트 동작 원리

Spring Cloud Bus는 **Pub/Sub (브로드캐스트) 패턴**을 사용하여 설정 갱신 이벤트를 전파합니다.

Config Server에서 `/actuator/busrefresh`를 호출하면, 각 클라이언트 인스턴스는 **여러 이벤트를 연쇄적으로 수신**하게 됩니다.

### RabbitMQ 큐 구조

Spring Cloud Bus는 각 인스턴스마다 **익명 큐(anonymous queue)**를 자동으로 생성합니다.

```
[Exchange: springCloudBus] (fanout type)
    ↓ binding
    ├─ [Queue] springCloudBus.anonymous.XXX1  ← Config Server
    ├─ [Queue] springCloudBus.anonymous.XXX2  ← Client :8080
    ├─ [Queue] springCloudBus.anonymous.XXX3  ← Client :8081
    └─ [Queue] springCloudBus.anonymous.XXX4  ← Client :8082
```

**익명 큐의 특징:**

- **임시 큐**: 애플리케이션 종료 시 자동 삭제 (auto-delete)
- **고유성**: 각 인스턴스마다 고유한 큐 생성
- **Pub/Sub 패턴**: 하나의 이벤트를 모든 인스턴스가 수신
- **자동 바인딩**: Exchange에 자동으로 바인딩

### 이벤트 전파 흐름

#### 1단계: Refresh 이벤트 발행

```
Config Server (/actuator/busrefresh 호출)
    ↓ Publish RefreshRemoteApplicationEvent
[RabbitMQ Exchange: springCloudBus]
    ↓ Fanout (모든 큐로 브로드캐스트)
    ├─ Config Server 큐 → 수신
    ├─ Client :8080 큐 → 수신 (설정 갱신 실행)
    ├─ Client :8081 큐 → 수신 (설정 갱신 실행)
    └─ Client :8082 큐 → 수신 (설정 갱신 실행)
```

#### 2단계: 각 인스턴스가 ACK 발행

설정 갱신을 완료한 각 인스턴스는 `AckRemoteApplicationEvent`를 발행합니다.

```
각 인스턴스가 설정 갱신 후 AckRemoteApplicationEvent 발행
    ↓ 다시 모든 인스턴스에게 브로드캐스트
[RabbitMQ Exchange: springCloudBus]
    ↓ Fanout
    ├─ Config Server 큐 → 수신 (로깅)
    ├─ Client :8080 큐 → 수신 (로깅)
    ├─ Client :8081 큐 → 수신 (로깅)
    └─ Client :8082 큐 → 수신 (로깅)
```

### 클라이언트 인스턴스의 이벤트 수신 순서

Config Server에서 `POST /actuator/busrefresh` 호출 시, 각 클라이언트 인스턴스는 다음 순서로 이벤트를 수신합니다:

#### 1. REFRESH EVENT RECEIVED (RefreshRemoteApplicationEvent)

```
╔═══════════════════════════════════════════════════════╗
║  REFRESH EVENT RECEIVED                               ║
╚═══════════════════════════════════════════════════════╝
Event Type              : CONFIGURATION REFRESH
Origin Service          : config-server:8888:xxx
Destination Service     : **
Event ID                : 60c624ab-ef1a-4b1e-b48b-d25f69523b91

→ @RefreshScope beans will be recreated
→ @ConfigurationProperties will be rebound
→ Latest config will be fetched from Config Server
```

**동작:**

- Config Server가 발행한 설정 갱신 이벤트 수신
- `@RefreshScope` 빈 재생성
- `@ConfigurationProperties` 재바인딩
- Config Server에서 최신 설정 조회

#### 2. REFRESH COMPLETED (자기 자신의 ACK)

```
╔═══════════════════════════════════════════════════════╗
║  REFRESH COMPLETED                                    ║
╚═══════════════════════════════════════════════════════╝
Event Type              : CONFIGURATION REFRESH COMPLETED
Origin service          : config-client:8080:xxx (자기 자신)
Ack ID                  : 60c624ab-ef1a-4b1e-b48b-d25f69523b91

✓ Configuration refresh completed successfully
✓ Application is now using the latest configuration
```

**동작:**

- 자기 자신이 설정 갱신을 완료했음을 알리는 ACK 이벤트
- 실제 처리: `isFromThisInstance()` 체크 후 완료 로직 실행

#### 3. REFRESH COMPLETED (Config Server의 ACK)

```
╔═══════════════════════════════════════════════════════╗
║  REFRESH COMPLETED                                    ║
╚═══════════════════════════════════════════════════════╝
Origin service          : config-server:8888:xxx
```

**동작:**

- Config Server가 발행한 ACK 이벤트 수신
- 추적/모니터링 목적으로 로깅만 수행

#### 4. REFRESH COMPLETED (다른 클라이언트 인스턴스들의 ACK)

```
╔═══════════════════════════════════════════════════════╗
║  REFRESH COMPLETED                                    ║
╚═══════════════════════════════════════════════════════╝
Origin service          : config-client:8081:xxx
Origin service          : config-client:8082:xxx
```

**동작:**

- 다른 클라이언트 인스턴스들이 발행한 ACK 이벤트 수신
- 추적/모니터링 목적으로 로깅만 수행

### 이벤트 필터링

로그의 DEBUG 메시지를 보면 각 이벤트마다 매칭 검사를 수행합니다:

```
In match: config-server:8888:xxx, config-client:8080:xxx
matchMultiProfile : config-server:8888:xxx, config-client:8080:xxx
```

각 인스턴스는:

- ✅ **자신에게 해당하는 이벤트만 실제 처리** (destination 매칭)
- 📝 **다른 인스턴스의 ACK는 수신만 하고 로깅** (추적/모니터링 목적)

### 이벤트 요약표

| 이벤트 타입                           | 발행자           | 수신자     | 처리 여부    | 목적        |
|----------------------------------|---------------|---------|----------|-----------|
| `RefreshRemoteApplicationEvent`  | Config Server | 모든 인스턴스 | ✅ 각자 처리  | 설정 갱신 트리거 |
| `AckRemoteApplicationEvent` (자신) | 자기 자신         | 모든 인스턴스 | ✅ 자신만 처리 | 완료 확인     |
| `AckRemoteApplicationEvent` (타인) | 다른 인스턴스       | 모든 인스턴스 | 📝 로깅만   | 분산 추적     |

### 분산 이벤트 추적

이것이 Spring Cloud Bus의 **분산 이벤트 추적(trace)** 메커니즘입니다.

`spring.cloud.bus.trace.enabled=true` 설정으로 인해:

- 모든 인스턴스가 모든 이벤트를 수신
- 각 인스턴스는 전체 시스템의 설정 갱신 상태를 파악 가능
- 디버깅 및 모니터링에 유용

**장점:**

- 설정 갱신이 모든 인스턴스에 성공적으로 전파되었는지 확인
- 어떤 인스턴스가 언제 설정을 갱신했는지 추적
- 문제 발생 시 빠른 원인 파악

**주의사항:**

- 인스턴스 수가 많으면 ACK 이벤트도 비례해서 증가
- 운영 환경에서는 로그 레벨 조정 권장 (INFO → WARN)

---

## 모니터링 및 추적

### Bus 이벤트 추적 (Tracing Bus Events)

Bus 이벤트(RemoteApplicationEvent의 하위 클래스)는 `spring.cloud.bus.trace.enabled=true`로 설정하여 추적할 수 있다.

이렇게 하면 Spring Boot TraceRepository(존재하는 경우)가 전송된 각 이벤트와 각 서비스 인스턴스의 모든 확인 응답(ack)을 보여준다.

**설정:**

```yaml
spring:
  cloud:
    bus:
      trace:
        enabled: true
```

다음 예제는 `/trace` 엔드포인트에서 가져온 것이다:

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

### 커스텀 이벤트 리스너로 추적

확인 신호(ack signal)를 직접 처리하려면 앱에 AckRemoteApplicationEvent 및 SentApplicationEvent 타입에 대한 @EventListener를 추가하면 된다.

**Server 이벤트 리스너 예시:**

```java

@Component
public class BusEventListener {
	@EventListener
	public void onSentApplicationEvent(SentApplicationEvent event) {
		log.info("=== Bus Event SENT ===");
		log.info("Type: {}", event.getType());
		log.info("Origin Service: {}", event.getOriginService());
		log.info("Destination Service: {}", event.getDestinationService());
	}

	@EventListener
	public void onAckRemoteApplicationEvent(AckRemoteApplicationEvent event) {
		log.info("=== Bus Event ACK ===");
		log.info("Origin Service: {}", event.getOriginService());
		log.info("Ack Destination: {}", event.getAckDestinationService());
	}

	@EventListener
	public void onRefreshRemoteApplicationEvent(RefreshRemoteApplicationEvent event) {
		log.info("=== Refresh Event Received ===");
		log.info("Origin Service: {}", event.getOriginService());
	}
}
```

**Client 이벤트 리스너 예시:**

```java

@Component
public class BusEventListener {
	@EventListener
	public void onRefreshRemoteApplicationEvent(RefreshRemoteApplicationEvent event) {
		log.info("║ CONFIGURATION REFRESH EVENT RECEIVED ║");
		log.info("→ @RefreshScope beans will be recreated");
		log.info("→ @ConfigurationProperties will be rebound");
		log.info("→ Latest config will be fetched from Config Server");

		// 메트릭 기록
		// meterRegistry.counter("config.refresh.received").increment();
	}

	@EventListener
	public void onAckRemoteApplicationEvent(AckRemoteApplicationEvent event) {
		if (isFromThisInstance(event)) {
			log.info("║ CONFIGURATION REFRESH COMPLETED ║");
			log.info("✓ Application is now using the latest configuration");

			// 갱신 후 설정 검증
			// configValidator.validateConfiguration();
		}
	}
}
```

모든 Bus 애플리케이션은 확인 응답을 추적할 수 있다.
그러나 때로는 데이터에 대해 더 복잡한 쿼리를 수행하거나 전문 추적 서비스로 전달할 수 있는 중앙 서비스에서 이 작업을 수행하는 것이 유용하다.

> Bus 이벤트 추적은 디버깅할 때 매우 유용하다.<br/>
> "내가 보낸 설정 갱신 이벤트가 정말로 모든 인스턴스에 도달했을까?"를 확인할 수 있다.<br/>
> 추적을 활성화하면 누가 이벤트를 보냈는지(`sent`), 누가 받았는지(`ack`)를 시간 순서대로 볼 수 있다.<br/>
> 운영 환경에서는 보통 비활성화하지만, 개발이나 문제 해결 시에는 활성화해서 사용한다.

---

## 베스트 프랙티스

### 1. 설정 관리

#### 권장: @ConfigurationProperties 사용

```java

@ConfigurationProperties(prefix = "app")
public class AppProperties {
	private String message;
	private Database database;

	@Getter
	@Setter
	public static class Database {
		private String url;
		private String username;
	}
}
```

**장점:**

- ✅ RefreshScope 불필요
- ✅ 타입 안전
- ✅ IDE 자동완성
- ✅ 구조화된 설정
- ✅ 자동 갱신

#### 비권장: @Value 남발

```java
// ❌ 지양
@Value("${app.database.url}")
private String dbUrl;

@Value("${app.database.username}")
private String dbUsername;
```

### 2. 커스텀 이벤트

#### Common 모듈에 이벤트 정의

```
project/
├── common/                          # 공통 모듈
│   └── events/
│       └── UserLoginEvent.java
├── server/                          # Config Server
│   └── dependencies: common
└── client/                          # Config Client
    └── dependencies: common
```

#### 필수 요구사항

- `RemoteApplicationEvent` 상속
- `@JsonTypeName` 어노테이션 추가
- no-arg 생성자 제공
- Producer와 Consumer 모두 동일한 클래스 정의

#### 스레드 안전성 고려

```java

@Component
public class CustomEventListener {
	// ✅ 스레드 안전
	private final Map<String, LoginInfo> recentLogins = new ConcurrentHashMap<>();

	@EventListener
	public void onUserLoginEvent(UserLoginEvent event) {
		recentLogins.put(event.getUsername(), loginInfo);
	}
}
```

### 3. 보안

#### actuator 엔드포인트 접근 제한

```yaml
management:
  endpoints:
    web:
      exposure:
        include: busrefresh, busenv, health, info
```

#### 네트워크 레벨 보호

```yaml
# Spring Security 설정
spring:
  security:
    user:
      name: admin
      password: ${ACTUATOR_PASSWORD}

management:
  server:
    port: 8081  # 별도 포트로 분리
```

#### 민감한 설정 암호화

```yaml
# Config Server에서 암호화된 값 사용
spring:
  datasource:
    password: '{cipher}AQA...'  # 암호화된 비밀번호
```

### 4. 모니터링

#### 갱신 이벤트 메트릭 수집

```java

@Component
public class BusEventListener {
	private final MeterRegistry meterRegistry;

	@EventListener
	public void onRefreshRemoteApplicationEvent(RefreshRemoteApplicationEvent event) {
		meterRegistry.counter("config.refresh.received",
		  "origin", event.getOriginService()
		).increment();
	}

	@EventListener
	public void onAckRemoteApplicationEvent(AckRemoteApplicationEvent event) {
		meterRegistry.counter("config.refresh.completed").increment();
	}
}
```

#### 설정 변경 이력 감사

```java

@Component
public class ConfigAuditListener {
	private final AuditService auditService;

	@EventListener
	public void onRefreshRemoteApplicationEvent(RefreshRemoteApplicationEvent event) {
		auditService.log(
		  "Config refresh triggered",
		  event.getOriginService(),
		  LocalDateTime.now()
		);
	}
}
```

### 5. 테스트

#### 로컬 테스트 환경

```bash
# Docker Compose로 RabbitMQ 실행
docker-compose up -d rabbitmq

# 1. run config server 
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-server:bootRun

# 2. run clients 
# 여러 인스턴스 실행
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun --args='--server.port=8080'
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun --args='--server.port=8081'
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun --args='--server.port=8082'

# 설정 갱신 테스트
curl -X POST http://localhost:8888/actuator/busrefresh

# 모든 인스턴스 확인
curl http://localhost:8080/api/config
curl http://localhost:8081/api/config
curl http://localhost:8082/api/config
```

#### 통합 테스트

```java

@SpringBootTest
@TestPropertySource(properties = {
  "spring.cloud.bus.enabled=true",
  "spring.rabbitmq.host=localhost"
})
class BusIntegrationTest {
	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Test
	void testCustomEventBroadcast() {
		// 커스텀 이벤트 발행
		UserLoginEvent event = new UserLoginEvent(...);
		eventPublisher.publishEvent(event);

		// 이벤트 수신 확인
		// ...
	}
}
```

### 6. 운영 환경 체크리스트

- [ ] RabbitMQ/Kafka 클러스터 구성
- [ ] actuator 엔드포인트 보안 설정
- [ ] 민감한 설정 암호화
- [ ] Service ID 고유성 보장
- [ ] Bus 이벤트 모니터링 설정
- [ ] 설정 변경 이력 감사 로그
- [ ] 장애 대응 플레이북 작성
- [ ] 롤백 전략 수립

---

## Reference

### 공식 문서

- [Spring Cloud Bus](https://docs.spring.io/spring-cloud-bus/reference/spring-cloud-bus.html)
- [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream)
- [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)

### 프로젝트 문서

- [설정 갱신 메커니즘 상세](./docs/refresh-scope-mechanisms.md) - @RefreshScope, Environment, @ConfigurationProperties 동작 원리
- [Spring Boot 3.x Actuator 설정](./docs/spring-boot-3-actuator-configuration.md) - Spring Boot 3에서 변경된 Actuator 설정 방법
- [프로젝트 설정 가이드](./docs/SETUP.md) - 로컬 환경 설정 및 테스트 방법
- [테스트 시나리오](./docs/test-scenarios.sh) - 실전 테스트 스크립트

### 추가 자료

- [Message Bus vs Message Queue](https://learn.microsoft.com/en-us/previous-versions/msp-n-p/dn589781(v=pandp.10))
