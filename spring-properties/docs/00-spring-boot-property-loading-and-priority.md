# Spring Boot 프로퍼티 설정

## Spring Boot 프로퍼티 기본 개념

- Spring Boot는 `application.properties` 또는 `application.yml` 파일을 통해 애플리케이션 설정을 관리한다.
- `src/main/resources`의 `application.properties`는 기본 설정 파일이며, 각 환경(profile)별로 `application-{profile}.properties` 또는 `.yml` 파일을 분리해 사용할 수 있다.
- 활성화할 프로파일은 `spring.profiles.active` 프로퍼티로 지정할 수 있다.

---

## 프로퍼티 로딩 전략

Spring Boot 2.4 이상부터는 설정 파일의 로딩 전략이 완전히 변경되었다.

### Spring Boot 2.3 이하

- 자동으로 `main/resources`의 `application.properties`에서 `test/resources`의 `application.properties` 순으로 **자동 병합**된다.
- 즉, 테스트 환경에서는 `main` 설정 파일의 값이 "기본값"처럼 상속되며, 테스트 설정 파일에서 필요한 값만 **덮어쓰는(override)** 구조다.

### Spring Boot 2.4 이상

- `spring.config.import` 프로퍼티에 명시적으로 지정한 파일만 로딩되는 방식으로 변경되었다.
    - 기존 자동 병합 방식은 더 이상 기본 동작이 아니다.
    - 필요한 경우 `import`를 사용해 원하는 설정 파일을 불러올 수 있다.
    - `import`에 명시된 파일의 순서에 따라 merge/override 우선순위를 조정할 수 있다.

> **중요:** 여러 설정 파일이 '병합'된다는 것은 **여러 설정 위치(예: 여러 classpath, 커맨드라인, 운영체제 환경변수 등)**에서 값을 불러와 병합할 수 있다는 의미다.

---

## Spring Boot 프로퍼티 소스 로딩 우선순위

Spring Boot는 다양한 설정 소스([`PropertySource`](https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/core/env/PropertySource.html))를 지원하며, 각 소스별 로딩 순서에 따라 우선순위가 적용된다.

뒤에 로딩되는 설정 소스일수록 앞서 로드된 동일 키의 값을 재정의(override)할 수 있다.

### 설정 소스 로딩 우선순위

아래로 내려갈수록 우선순위가 높으며, 동일한 키가 존재하면 더 나중에 로딩된 설정이 최종 값을 갖는다.

1. Java 코드 내 기본 설정
    - **`SpringApplication.setDefaultProperties(Map)`**로 지정한 기본값
2. `@Configuration` 클래스 내 `@PropertySource` 어노테이션
    - 해당 설정은 `ApplicationContext` 초기화 시점에 추가됨
    - 단, `logging.*`, `spring.main.*`, `spring.application.name` 등 부트스트랩 단계 설정에는 적용되지 않는다.
    - `@PropertySource`는 초기 설정용이 아니라, `@Value`, `Environment.getProperty()` 등으로 **사용자 정의 설정값**을 불러올 때 적합하다.
3. 설정 파일 (`application.properties`, `application.yml`)
4. [RandomValuePropertySource](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/env/RandomValuePropertySource.html)  
   랜덤값(`random.*` 등, 예: UUID, 포트)
5. [운영체제 환경 변수(Environment Variables)](#os-환경-변수)
6. Java 시스템 프로퍼티 (`-Dkey=value` 형식)
7. JNDI 속성 (`java:comp/env`)
8. `ServletContext` 초기화 파라미터
9. `ServletConfig` 초기화 파라미터
10. [`SPRING_APPLICATION_JSON` 환경변수 또는 시스템 프로퍼티](#json-기반-application-properties)
    - JSON 형식으로 설정값 지정 가능
11. [명령줄 인자(Command-line arguments)](#커맨드라인-프로퍼티-접근)
    - 예시: `--server.port=9090`
12. 테스트에서 지정한 [`@SpringBootTest(properties = …)`](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#testing.spring-boot-applications.autoconfigured-tests)
13. 테스트 클래스 내 `@DynamicPropertySource`
14. `@TestPropertySource`
15. [DevTools 전역 설정](https://docs.spring.io/spring-boot/reference/using/devtools.html#using.devtools.globalsettings)
    - `$HOME/.config/spring-boot` 경로에 위치하며, DevTools 활성화 시에만 적용됨

### 설정 파일(Config Data) 로딩 우선순위

Spring Boot의 `application.properties` 및 `application.yml` 파일도 아래와 같은 우선순위로 로딩된다.

1. JAR 내부에 포함된 `application.properties` 또는 `.yml`
2. JAR 내부에 포함된 프로파일 전용 파일 (`application-{profile}.properties`)
3. 외부 경로에 위치한 `application.properties` 또는 `.yml`
4. 외부 경로에 위치한 프로파일 전용 파일

> 참고: 동일한 경로에 `.properties`와 `.yml` 파일이 모두 존재하면, `.properties` 파일이 우선 적용된다.

---

## OS 환경 변수

운영체제 환경 변수에는 점(`.`) 문자를 사용할 수 없으므로, 언더스코어(`_`)로 대체하여 지정해야 한다.

예를 들어, 시스템 프로퍼티(`-Dspring.config.name=myapp`) 대신 환경 변수에서는 `SPRING_CONFIG_NAME`을 사용한다.

자세한 내용은 [Binding From Environment Variables](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables)를 참고하자.

---

## [JSON 기반 Application Properties](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.application-json)

환경 변수나 시스템 프로퍼티에는 사용할 수 있는 프로퍼티 이름에 제약이 많은 경우가 있다. 이를 보완하기 위해 Spring Boot는 여러 프로퍼티들을 하나의 JSON 구조로 인코딩해서 한 번에 넘기는 방식을 지원한다.

애플리케이션이 시작될 때, `spring.application.json` 또는 `SPRING_APPLICATION_JSON` 프로퍼티에 포함된 JSON 데이터가 파싱되어 Environment(환경)에 추가된다.

예를 들어, UNIX 계열 쉘에서 `SPRING_APPLICATION_JSON` 환경 변수를 명령줄로 전달할 수 있다.

```shell
$ SPRING_APPLICATION_JSON='{"my":{"name":"test"}}' java -jar myapp.jar
```

위 예시에서는 `my.name=test` 프로퍼티가 Spring Environment에 추가된다.
동일한 JSON을 시스템 프로퍼티로도 지정할 수 있다.

```shell
$ java -Dspring.application.json='{"my":{"name":"test"}}' -jar myapp.jar
```

또는 커맨드라인 인수로 전달해도 된다.

```shell
$ java -jar myapp.jar --spring.application.json='{"my":{"name":"test"}}'
```

클래식 Application Server에 배포하는 경우라면 JNDI 변수 `java:comp/env/spring.application.json`을 사용할 수도 있다.

JSON에 포함된 값이 `null`이라면 해당 값 역시 프로퍼티 소스에 추가된다. 하지만 `PropertySourcesPropertyResolver`는 `null` 값을 가진 프로퍼티를 존재하지 않는 값으로 처리한다. 즉, JSON을 통해 `null`을 설정해도, 우선순위가 낮은 프로퍼티 소스의 값을 `null`로 덮어쓸 수는 없다.

---

## [커맨드라인 프로퍼티 접근](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.command-line-args)

기본적으로 `SpringApplication`은 커맨드라인에서 전달된 옵션 인자(예시: `--server.port=9000`과 같이 `--`로 시작하는 인자)를 프로퍼티로 변환하여 Spring `Environment`에 추가한다. 위에서 언급했듯이, **커맨드라인에서 전달한 프로퍼티는 항상 파일 기반 프로퍼티 소스보다 높은 우선순위를 가진다.**

만약 커맨드라인 프로퍼티가 `Environment`에 추가되는 것을 원치 않는다면, `SpringApplication.setAddCommandLineProperties(false)` 메서드를 사용하여 비활성화할 수 있다.

---

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
