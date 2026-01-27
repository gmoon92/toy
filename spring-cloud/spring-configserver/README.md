# [Spring Cloud Config](https://spring.io/projects/spring-cloud)

Spring Cloud Config는 분산 시스템에서 외부화된 설정을 관리하기 위한 서버 및 클라이언트 측 지원을 제공합니다.

Config Server를 통해 모든 환경의 애플리케이션에 대한 외부 속성을 중앙에서 관리할 수 있습니다.

## 개요

Spring Cloud Config의 클라이언트와 서버 개념은

Spring의 `Environment`와 `PropertySource` 추상화와 동일하게 매핑되어 Spring 애플리케이션과 완벽하게 통합되지만, 모든 언어로 실행되는 애플리케이션에서도 사용할 수 있습니다.
애플리케이션이 개발(dev), 테스트(test), 운영(production) 환경으로 배포 파이프라인을 거쳐갈 때, 각 환경 간의 설정을 관리하고 애플리케이션이 마이그레이션 시 필요한 모든 것을 갖추도록 보장할 수
있습니다.

서버 스토리지 백엔드의 기본 구현은 Git을 사용하므로 라벨이 지정된 버전의 설정 환경을 쉽게 지원하며,
콘텐츠 관리를 위한 다양한 도구에서 접근할 수 있습니다. 대체 구현을 추가하고 Spring 설정으로 연결하는 것도 간단합니다.

---

## 🚀 Quick Start

```bash
# 1. Config Server 실행 (Port 8888)
./gradlew :spring-cloud:spring-configserver:spring-configserver-server:bootRun

# 2. Config Client 실행 (Port 8080)
./gradlew :spring-cloud:spring-configserver:spring-configserver-client:bootRun

# 3. 현재 설정 확인
curl http://localhost:8080/config

# 4. 설정 파일 수정
#  spring-configserver-client 모듈의 application.yml 설정 값 확인
#  spring.application.name(client), label(alpha), profile에 따라 설정 파일 경로 결정
#  예시: spring-configserver-server/src/main/resources/config-repo/alpha/client.yml

# 5. 설정 갱신 (재시작 없이)
curl -X POST http://localhost:8080/actuator/refresh

# 6. 변경된 설정 확인
curl http://localhost:8080/config
```

---

## 아키텍처 설계

```text
┌─────────────────────────────────────────┐
│   spring-configserver-server            │
│   (Config Server - Port 8888)           │
│                                         │
│   - Native FileSystem에서 설정 제공         │
│   - classpath:/config-repo              │
│   - /{application}/{profile} 엔드포인트    │
└─────────────────────────────────────────┘
          ↓ 설정 조회 (http://localhost:8888)
┌──────────────────────────────────────────────┐
│   client                 │
│   (Config Client - Port 8080)                │
│                                              │
│   - 시작 시 Config Server에서 설정 로드           │
│   - @RefreshScope + @ConfigurationProperties │
│   - /actuator/refresh POST 엔드포인트           │
└──────────────────────────────────────────────┘
```

### 동작 흐름

1. **초기 로딩**: Client 애플리케이션 시작 → Config Server에서 설정 가져옴
2. **설정 변경**: Config Server의 설정 파일 수정 (classpath:/config-repo/*.yml)
3. **갱신 트리거**: `POST http://localhost:8080/actuator/refresh` 호출
4. **빈 재생성**: `@RefreshScope`가 붙은 빈들만 새 설정으로 재생성 (Tomcat 재시작 X)

---

## 주요 기능

- [Config Server (spring-configserver-server)](#config-server-spring-configserver-server)
- [Config Client (client)](#config-client-client)

### Config Server (spring-configserver-server)

- `@EnableConfigServer` 어노테이션을 사용하여 Spring Boot 애플리케이션에 쉽게 임베딩
    ```java
    @EnableConfigServer // `@EnableConfigServer` 어노테이션으로 Config Server 활성화
    @SpringBootApplication
    public class SpringConfigserverServerApplication {
        static void main(String[] args) {
            SpringApplication.run(SpringConfigserverServerApplication.class, args);
        }
    }
    ```
- Native FileSystem 설정 저장소 사용 (`classpath:/config-repo`)
  ```properties
  # https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_file_system_backend
  # 'native' 는 설정값을 로컬 파일 시스템에서 직접 읽어오도록 하는 특별한 프로파일로
  # Spring Cloud Config Server는
  # Git 저장소 대신, 설정 파일이 저장된 로컬 디렉토리(예: file:///path/to/config-repo)를 바라봅니다.
  spring.profiles.active=native
    
  spring.cloud.config.server.native.search-locations=classpath:/config-repo
  ```
    - **현재**: Native FileSystem 사용 (로컬 개발 환경에 적합)
        - 설정 파일 위치: `spring-configserver-server/src/main/resources/config-repo`
        - 빠른 프로토타이핑 및 테스트에 유리
    - **운영 환경**: Git 저장소 사용 권장
        - 버전 관리 및 변경 이력 추적
        - 롤백 기능
        - 팀 협업에 유리
- HTTP 기반 리소스 API를 통한 외부 설정 제공 (name-value 쌍 또는 YAML 형식)
    - 설정 파일 규칙: `{application-name}.yml`, `{application-name}-{profile}.yml`

### Config Client (client)

- Config Server에 바인딩하여 원격 속성 소스로 Spring Environment 초기화
    ```properties
    #`spring.config.import` 설정으로 Config Server 연결
    spring.config.import="optional:configserver:http://localhost:8888"
  
    # Spring Boot Actuator의 refresh 엔드포인트 활성화
    management.endpoints.web.exposure.include= refresh
    ```
- `/actuator/refresh` 엔드포인트를 통한 런타임 설정 갱신
- `@RefreshScope` + `@ConfigurationProperties` 조합으로 런타임 갱신 지원
    ```java
    @RefreshScope
    @Component
    @ConfigurationProperties(prefix = "app")
    public class AppProperties {
    	private String message;
    	private String version;
    	// getters and setters
    }
    ```
    - `@Value`보다 타입 안전성 우수
    - 설정 값 검증 기능 제공 (Jakarta Validation)
    - 구조화된 설정 관리
- 프로파일별 설정 관리
  ```properties
  # 환경별로 설정 파일을 분리하여 관리
  # 개발(dev), 운영(prod)
  spring.profiles.active=dev
  ```
    - `client.yml` (공통 설정)
    - `client-dev.yml` (개발 환경)
    - `client-prod.yml` (운영 환경)

---

## 동작 방식

### Config Server EndPoint

| 엔드포인트 패턴                               | 설명                                          |
|----------------------------------------|---------------------------------------------|
| `/{application}/{profile}`             | 특정 애플리케이션의 프로파일별 설정 데이터 (쉼표로 여러 프로파일 지정 가능) |
| `/{application}/{profile}/{label}`     | Git 라벨(브랜치/태그)을 포함한 설정 데이터                  |
| `/{label}/{application}-{profile}.yml` | 환경별 설정 파일을 직접 조회                            |

```bash
# 기본 프로파일 설정 조회
curl http://localhost:8888/client/default

## /{application}/{profile}/{label}
curl http://localhost:8888/client/default/alpha
curl http://localhost:8888/client/dev/alpha
curl http://localhost:8888/client/prod/alpha

curl http://localhost:8888/client/default/real
curl http://localhost:8888/client/dev/real
curl http://localhost:8888/client/prod/real

# /{label}/{application}-{profile}.yml
curl http://localhost:8888/alpha/client-default.yml
curl http://localhost:8888/alpha/client-dev.yml
curl http://localhost:8888/alpha/client-alpha.yml

curl http://localhost:8888/real/client-default.yml
curl http://localhost:8888/real/client-dev.yml
curl http://localhost:8888/real/client-alpha.yml
```

> 참고: https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_quick_start

### 설정 갱신 프로세스

1. Config Server의 설정 파일 수정
2. `/actuator/refresh` 엔드포인트 호출
3. 변경된 설정 키만 감지
4. `@RefreshScope` 빈 재생성
5. 애플리케이션 재시작 없이 새 설정 적용

---

## 주의사항 & 제한사항

- [의존성 및 버전 호환성](#의존성-및-버전-호환성)
- [Refresh 가능한 것](#refresh-가능한-것)
- [Refresh 불가능한 것](#refresh-불가능한-것)
- [권장 사항](#권장-사항)

### 의존성 및 버전 호환성

Spring Boot 버전과 호환되는 Spring Cloud 버전을 사용해야 합니다.
버전 호환성은 [링크](https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions#supported-releases) 를 참고해주세요.

**버전 불일치 오류 예시:**

```text
***************************
APPLICATION FAILED TO START
***************************

Description:

Your project setup is incompatible with our requirements due to following reasons:

- Spring Boot [3.5.0] is not compatible with this Spring Cloud release train


Action:

Consider applying the following actions:

- Change Spring Boot version to one of the following versions [3.4.x] .
  You can find the latest Spring Boot versions here [https://spring.io/projects/spring-boot#learn].
  If you want to learn more about the Spring Cloud Release train compatibility, you can visit this page [https://spring.io/projects/spring-cloud#overview] and check the [Release Trains] section.
  If you want to disable this check, just set the property [spring.cloud.compatibility-verifier.enabled=false]
```

### Refresh 가능한 것

- `@RefreshScope` 어노테이션이 붙은 빈
- `@ConfigurationProperties` 빈
- `Environment` 프로퍼티

### Refresh 불가능한 것

- `@Value` 필드 주입 (일반 싱글톤 빈에서)
- `DataSource`, JPA 설정 등 인프라 빈
- 이미 초기화된 스레드풀, 커넥션풀
- `@PostConstruct`에서 초기화된 상태

### 권장 사항

1. 런타임 갱신이 필요한 설정은 `@RefreshScope` + `@ConfigurationProperties` 사용
2. 인프라 관련 설정은 애플리케이션 재시작 필요
3. 민감한 정보는 암호화 사용 권장

---

## Reference

- [Spring Cloud Config 공식 문서](https://spring.io/projects/spring-cloud-config)
    - [Config Server 샘플](https://github.com/spring-cloud-samples/configserver)
    - [Config Client 샘플](https://github.com/spring-cloud-samples/customers-stores)
- [Spring Cloud 버전 호환성](https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions)

---

## Todo: 향후 개선 사항

1. [Git 저장소 전환](#1-git-저장소-전환)
2. [Spring Cloud Bus 도입](#2-spring-cloud-bus-도입)
3. [Refresh Hook 구현](#3-refresh-hook-구현)
4. [설정 암호화](#4-설정-암호화)

### 1. Git 저장소 전환

운영 환경 적용 시 Git 기반 설정 관리로 변경 예정

- 버전 관리 및 변경 이력 추적
- 롤백 기능
- 브랜치/태그를 활용한 환경별 관리

### 2. Spring Cloud Bus 도입

Config Server는 설정이 변경되더라도 이를 클라이언트에게 자동으로 푸시하지 않는다.

- Config Server는 설정이 바뀌어도 클라이언트에게 자동으로 알리지 않는다.
- Config Server는 설정 저장소이자 조회 API 역할을 수행한다.
- 따라서 설정 변경 사항은 클라이언트가 다시 조회해야만 반영된다.

이로 인해 설정 변경을 시스템 전체에 반영하기 위한 별도의 "갱신 트리거" 메커니즘이 필요하다.

**해결 방안**:

- Spring Cloud Bus + Message Broker (RabbitMQ/Kafka)
- `/actuator/busrefresh` 한 번 호출로 전체 인스턴스 갱신

자세한 내용은 [Spring Cloud Bus 문서](../spring-cloud-bus/README.md)를 참고하자.

### 3. Refresh Hook 구현

갱신 전후 로직 처리를 위한 이벤트 리스너 구현 고려 (web hook alarm)

```java

@Component
public class RefreshEventListener {

	@EventListener
	public void onRefresh(RefreshScopeRefreshedEvent event) {
		// 설정 갱신 후 처리 로직
		log.info("Configuration refreshed: {}", event.getName());
	}
}
```

### 4. 설정 암호화

민감한 정보 보호를 위한 암호화 기능 추가 고려

- 대칭키 또는 비대칭키 암호화
- `{cipher}` 접두사를 사용한 암호화된 값 관리
