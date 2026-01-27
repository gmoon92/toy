# Spring Cloud Config Refresh 메커니즘 상세 분석

## 핵심 질문

> ConfigController에 `@RefreshScope`를 선언한 이유는 `@Value`로 주입된 필드값을 갱신하기 위함인데,
> Environment도 싱글톤이잖아? 왜 Environment는 갱신된 값을 가져올 수 있는가?
> @ConfigurationProperties는 프록시 빈으로 등록되어 설정 값 lazy lookup 동작 방식이 아닌가?

## 세 가지 설정 접근 방식 비교

### 1. @Value (eager binding - RefreshScope 필요)

```java

@RefreshScope  // ← 없으면 갱신 안 됨!
@Component
public class MyService {
	@Value("${app.message}")
	private String message;  // 빈 생성 시 주입되고 고정됨
}
```

**동작 방식**:

```
빈 생성 시점:
  Environment.getProperty("app.message") → "Hello"
  messageFromValue = "Hello"  // 필드에 저장
    ↓
이후 호출:
  return messageFromValue;  // "Hello" (고정된 값)
    ↓
/actuator/refresh 호출:
  RefreshScope 캐시 클리어
    ↓
다음 빈 접근:
  새 빈 생성
  Environment.getProperty("app.message") → "Updated!"
  messageFromValue = "Updated!"  // 새 값으로 재주입
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

**동작 방식**:

```
호출 시점마다:
  Environment.getProperty("app.message")
    ↓
  PropertySource에서 실시간 조회
    ↓
  현재 값 반환 (항상 최신)
```

### 3. @ConfigurationProperties (rebinding - RefreshScope 불필요)

```java

@ConfigurationProperties(prefix = "app")  // ← RefreshScope 불필요!
public class AppProperties {
	private String message;  // 일반 필드 (프록시 아님!)
}
```

**동작 방식**:

```
초기 바인딩:
  Environment.getProperty("app.message") → "Hello"
  properties.message = "Hello"
    ↓
/actuator/refresh 호출:
  ConfigurationPropertiesRebinder.rebind()
    ↓
  Environment.getProperty("app.message") → "Updated!"
  properties.message = "Updated!"  // 필드 재바인딩
```

---

## 상세 비교표

| 방식                         | 바인딩 시점         | RefreshScope 필요 | 갱신 메커니즘        | 프록시 여부 |
|----------------------------|----------------|-----------------|----------------|--------|
| `@Value`                   | 빈 생성 시 (eager) | ✅ 필수            | 빈 재생성          | ❌      |
| `Environment`              | 호출 시 (lazy)    | ❌ 불필요           | 실시간 조회         | ❌      |
| `@ConfigurationProperties` | 초기 + Rebind 시  | ❌ 불필요           | Rebinder가 재바인딩 | ❌      |

---

## Environment가 갱신되는 이유

### Environment의 구조

```java
public interface Environment {
	String getProperty(String key);  // 호출 시점에 조회
}

// 실제 구현
public class StandardEnvironment extends AbstractEnvironment {
	private MutablePropertySources propertySources;  // 여러 PropertySource
}
```

### PropertySource 갱신 흐름

```
/actuator/refresh 호출
    ↓
ContextRefresher.refresh()
    ↓
Environment의 PropertySource 교체
    ↓
기존: [ConfigService, application.yml, System]
새로: [ConfigService(갱신됨), application.yml, System]
    ↓
Environment.getProperty() 호출
    ↓
새로운 PropertySource에서 값 조회 → 갱신된 값 반환
```

**핵심**: Environment는 **PropertySource를 참조**하므로, PropertySource가 교체되면 자동으로 새 값 반환

---

## @ConfigurationProperties는 프록시가 아니다!

### 오해: 프록시로 lazy하게 값을 가져온다?

```java

@ConfigurationProperties(prefix = "app")
public class AppProperties {
	private String message;  // 일반 필드!

	public String getMessage() {
		return message;  // 그냥 필드 값 반환 (프록시 아님)
	}
}
```

**실제 동작**:

- `AppProperties`는 **일반 POJO**
- 프록시 없음
- 필드에 값이 저장됨

### 진실: ConfigurationPropertiesRebinder가 재바인딩

```java
// Spring Cloud Context 내부 코드
@Component
public class ConfigurationPropertiesRebinder
  implements ApplicationListener<EnvironmentChangeEvent> {

	@Override
	public void onApplicationEvent(EnvironmentChangeEvent event) {
		rebind();  // 환경 변경 이벤트 감지
	}

	public void rebind() {
		for (String name : this.beans.getBeanNames()) {
			rebind(name);  // 각 @ConfigurationProperties 빈 재바인딩
		}
	}

	private void rebind(String name) {
		// 1. 기존 빈 가져오기
		AppProperties bean = context.getBean(name, AppProperties.class);

		// 2. Environment에서 새 값 읽기
		String newMessage = environment.getProperty("app.message");

		// 3. 필드 재설정 (리플렉션 사용)
		bean.message = newMessage;  // 재바인딩!
	}
}
```

---

## 실제 코드 예시로 비교

### ConfigController에 Environment 추가

```java

@Slf4j
@RefreshScope  // @Value 때문에 필요
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConfigController {

	private final AppProperties configProperties;
	private final Environment environment;  // 추가

	@Value("${app.config.message:Default from @Value}")
	private String messageFromValue;

	@GetMapping("/config")
	public Map<String, Object> getConfig() {
		Map<String, Object> response = new HashMap<>();

		// 1. @Value (RefreshScope 필요)
		response.put("valueInjection", Map.of(
		  "message", messageFromValue,
		  "explanation", "빈 생성 시 주입, RefreshScope로 재생성 시 갱신"
		));

		// 2. Environment (RefreshScope 불필요)
		response.put("environment", Map.of(
		  "message", environment.getProperty("app.config.message"),
		  "explanation", "호출 시마다 실시간 조회, 항상 최신 값"
		));

		// 3. @ConfigurationProperties (RefreshScope 불필요)
		response.put("configProperties", Map.of(
		  "message", configProperties.getMessage(),
		  "explanation", "Rebinder가 자동 재바인딩, RefreshScope 불필요"
		));

		return response;
	}
}
```

### 갱신 전후 비교

```bash
# 초기 상태 (app.config.message = "Hello")
curl http://localhost:8080/api/config
{
  "valueInjection": {"message": "Hello"},
  "environment": {"message": "Hello"},
  "configProperties": {"message": "Hello"}
}

# 설정 변경 (app.config.message = "Updated!")
# Git 저장소 업데이트 후

# refresh 전 (캐시된 빈)
curl http://localhost:8080/api/config
{
  "valueInjection": {"message": "Hello"},        # 고정됨
  "environment": {"message": "Updated!"},        # 이미 갱신!
  "configProperties": {"message": "Hello"}       # 아직 안 갱신
}

# refresh 호출
curl -X POST http://localhost:8888/actuator/busrefresh

# refresh 후
curl http://localhost:8080/api/config
{
  "valueInjection": {"message": "Updated!"},     # 빈 재생성으로 갱신
  "environment": {"message": "Updated!"},        # 계속 최신 값
  "configProperties": {"message": "Updated!"}    # Rebinder로 갱신
}
```

---

## RefreshScope 프록시 메커니즘

### @RefreshScope는 프록시를 생성한다

```java

@RefreshScope
@Component
public class MyService {
	@Value("${app.message}")
	private String message;
}
```

**실제 빈 구조**:

```
MyService$$EnhancerBySpringCGLIB$$12345678 (프록시)
    ↓
ScopedProxyFactoryBean
    ↓
RefreshScope 캐시
    ↓
실제 MyService 인스턴스 (message = "Hello")
```

### 프록시 동작

```java
// 클라이언트 코드
myService.doSomething();

// 실제 호출 흐름
MyService$$Proxy.

doSomething()
    ↓
	  RefreshScope.

get("myService")  // 캐시에서 조회
    ↓ (
캐시 존재)
실제 인스턴스.

doSomething()

// refresh 후
MyService$$Proxy.

doSomething()
    ↓
	  RefreshScope.

get("myService")  // 캐시에서 조회
    ↓ (
캐시 비어있음)
새 인스턴스

생성(message ="Updated!")
    ↓
새 인스턴스.

doSomething()
```

---

## 왜 @ConfigurationProperties는 프록시가 아닌가?

### ConfigurationPropertiesBean 등록 과정

```java
// Spring Boot 내부
@EnableConfigurationProperties(AppProperties.class)

// 실제 처리
ConfigurationPropertiesBindingPostProcessor
    ↓
일반 빈으로

등록(프록시 없음)
    ↓
초기 바인딩
    ↓
ConfigurationPropertiesRebinder에 등록
```

**프록시가 필요 없는 이유**:

1. 필드 값이 변경 가능 (mutable)
2. Rebinder가 직접 필드를 재설정
3. lazy 조회가 아니라 rebind 방식

---

## Environment vs @ConfigurationProperties 차이

### Environment 장점

```java
public String getMessage() {
	return environment.getProperty("app.message");  // 항상 최신
}
```

- ✅ RefreshScope 불필요
- ✅ refresh 전에도 최신 값
- ❌ 타입 안전성 없음
- ❌ IDE 자동완성 없음
- ❌ 매번 문자열 키 입력

### @ConfigurationProperties 장점

```java
public String getMessage() {
	return configProperties.getMessage();  // 타입 안전
}
```

- ✅ RefreshScope 불필요
- ✅ 타입 안전
- ✅ IDE 자동완성
- ✅ 구조화된 설정
- ⚠️ refresh 호출 전까지는 이전 값

---

## 실전 권장 사항

### 1. 일반적인 설정: @ConfigurationProperties

```java

@ConfigurationProperties(prefix = "app")
public class AppProperties {
	private String message;
	private Database database;
	// getter/setter
}
```

**이유**: 타입 안전, 구조화, 자동 갱신

### 2. 동적 조회가 필요한 경우: Environment

```java

@Component
public class DynamicConfigService {
	private final Environment environment;

	public String getValue(String key) {
		return environment.getProperty(key);  // 동적 키
	}
}
```

**이유**: refresh 전에도 최신 값, 동적 키 지원

### 3. 간단한 값 주입: @Value + @RefreshScope

```java

@RefreshScope
@Component
public class SimpleService {
	@Value("${app.flag:false}")
	private boolean featureFlag;
}
```

**이유**: 간단한 값, 구조화 불필요

### 4. @Value는 가급적 피하기

```java
// ❌ 지양
@Value("${app.database.url}")
private String dbUrl;

@Value("${app.database.username}")
private String dbUsername;

// ✅ 권장
@ConfigurationProperties(prefix = "app.database")
public class DatabaseProperties {
	private String url;
	private String username;
}
```

---

## 정리

### @Value가 갱신 안 되는 이유

- 빈 생성 시점에 값이 **필드에 저장**됨
- 이후 필드 값만 참조 (Environment 조회 안 함)
- → **RefreshScope로 빈 재생성 필요**

### Environment가 갱신되는 이유

- 호출 시마다 **PropertySource 조회**
- PropertySource가 교체되면 자동 반영
- → **RefreshScope 불필요**

### @ConfigurationProperties가 갱신되는 이유

- 필드에 값이 저장되지만 (프록시 아님)
- **ConfigurationPropertiesRebinder**가 감지하고 재바인딩
- → **RefreshScope 불필요**

### 핵심 차이

|                          | 저장 방식          | 갱신 메커니즘           | RefreshScope |
|--------------------------|----------------|-------------------|--------------|
| @Value                   | 필드 저장 (eager)  | 빈 재생성             | ✅ 필요         |
| Environment              | 매번 조회 (lazy)   | PropertySource 교체 | ❌ 불필요        |
| @ConfigurationProperties | 필드 저장 + Rebind | Rebinder 재바인딩     | ❌ 불필요        |

---

## 참고: Spring Cloud Context 내부 코드

### ContextRefresher.refresh()

```java
public synchronized Set<String> refresh() {
	// 1. Environment 갱신
	Set<String> keys = refreshEnvironment();

	// 2. RefreshScope 캐시 클리어
	this.scope.refreshAll();

	return keys;
}

public Set<String> refreshEnvironment() {
	// PropertySource 교체
	Map<String, Object> before = extract(environment.getPropertySources());
	addConfigFilesToEnvironment();
	Set<String> keys = changes(before, extract(environment.getPropertySources()));

	// EnvironmentChangeEvent 발행 → ConfigurationPropertiesRebinder 트리거
	this.context.publishEvent(new EnvironmentChangeEvent(keys));

	return keys;
}
```

### 흐름도

```
/actuator/refresh
    ↓
ContextRefresher.refresh()
    ↓
┌─────────────────────────────────────┐
│ 1. refreshEnvironment()              │
│    - PropertySource 교체             │
│    - EnvironmentChangeEvent 발행     │
│    → Environment 갱신 완료           │
├─────────────────────────────────────┤
│ 2. EnvironmentChangeEvent 처리       │
│    → ConfigurationPropertiesRebinder │
│    → @ConfigurationProperties 재바인딩│
├─────────────────────────────────────┤
│ 3. scope.refreshAll()               │
│    → RefreshScope 캐시 클리어        │
│    → @RefreshScope 빈 재생성 준비    │
└─────────────────────────────────────┘
```
