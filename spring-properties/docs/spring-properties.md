# Spring Boot 프로퍼티 설정

## Spring Boot 프로퍼티 기본 개념

- Spring Boot는 `application.properties` 또는 `application.yml` 파일을 통해 설정을 관리한다.
- `src/main/resources`의 `application.properties`는 기본 설정이며, `src/main/resources/application-{profile}.properties` 또는
  `.yml` 파일로 프로파일별 설정을 분리 가능하다.
- `spring.profiles.active` 프로퍼티로 활성 프로파일 지정.

---

## Property 로딩 전략

Spring boot 2.4 이상부터는 설정 파일의 로딩 전략이 완전히 바뀌었다.

### Spring Boot 2.3 이하

- 자동으로 main의 `application.properties` → test의 `application.properties` 순으로 **자동 병합**
- 즉, test에서는 main 설정은 "기본값"으로 상속받고, test 설정에서 필요한 값만 **덮어쓰는 구조**

### Spring Boot 2.4+

- `spring.config.import` 속성에 명시한 파일만 로딩되도록 변경되었다.
    - 기본 자동 로딩 병합 체계는 더이상 동작하지 않음.
    - 추가 import가 일어난다.
    - merge/override 우선순위를 변경할 수 있다.

병합되는 상황은 **Spring Boot가 여러 configuration location에서*- 병합하는 경우(예: classpath 여러 곳, 커맨드라인 등)이고,  
**test/resources와 main/resources 관계는 "덮어쓰기(override)", 병합(merge) 아님**

단, 아래와 같이 작성하면 정말 **기본설정 완전 배제**임

```properties
spring.config.import=classpath:/my-own-config.yml
```

```
# test/resources/application.properties
spring.config.import=classpath:application-core.properties # main 설정을 명시적으로 import
```

### `spring.config.import` 지원 경로 prefix

| Prefix        | 설명                                        |
|---------------|-------------------------------------------|
| `classpath:`  | 상대 경로 (현재 클래스패스 기준)                       |
| `classpath:/` | 절대 경로 (루트 클래스패스 기준)                       |
| `file:`       | 파일 시스템 경로 (절대/상대 경로)                      |
| `optional:`   | 경로에 파일이 없어도 예외 발생하지 않음.                   |
| `configtree:` | 파일 내용이 설정 값이 되는 특수 경로 (Kubernetes 등에서 사용) |
| `jar:`        | JAR 내부의 경로 지정 (복잡하므로 거의 안 씀)              |

- `classpath:`는 테스트 실행 시 `test/resources`가 우선 탐색되므로,
  같은 파일명이면 `test/resources`가 main 보다 우선 로드되어 main 파일은 가려짐.
- `classpath:/`와 `classpath:` 차이는 절대경로냐 상대경로냐 차이지만, 결국 클래스패스 우선순위 규칙은 동일.

---

## Spring Boot 프로퍼티 소스 로딩 우선순위

Spring Boot는 다양한 설정 소스([
`PropertySource`](https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/core/env/PropertySource.html))
를 지원하며, 각 소스는 로딩 순서에 따라 우선순위가 적용된다.

뒤에서 로딩된 설정은 앞에서 정의된 설정 값을 재정의(overriding)할 수 있다.

### 설정 소스 로딩 우선순위

다음은 Spring Boot가 설정 값을 읽어들이는 순서이다. 아래로 내려갈수록 우선순위가 높으며, 동일한 키가 존재할 경우 더 나중에 로딩된 설정이 최종 값을 갖는다.

1. 기본 설정 (Java 코드 내 설정)
    - **`SpringApplication.setDefaultProperties(Map)`** 을 통해 명시적으로 설정한 기본값
2. `@Configuration` 클래스 내 `@PropertySource` 어노테이션
    - 해당 설정은 `ApplicationContext`가 초기화되는 시점에 추가된다.
    - 단 `logging.*`이나 `spring.main.*`과 같이 초기 부트스트랩 단계에서 사용되는 설정에는 적용되지 않는다.
        - `logging.level.*`, `spring.main.banner-mode`, `spring.application.name` 같은 설정 값들은 `SpringApplication.run()` 이
          호출되는 "매우 초기 단계"에서 이미 사용되기 때문에, 나중에 @PropertySource로 값을 설정해도 이미 늦은 상태라는 뜻
    - `@PropertySource`는 초기 설정용이 아닌, `@Value`, `Environment.getProperty()` 등을 통해 **사용자 정의 설정값을 불러올 때 적합하다.**
    - `logging.*`, `spring.main.*`, `spring.application.name`과 같은 초기 구동 단계의 필수 설정은 반드시 `.properties`, `.yml`, JVM 옵션 등으로
      제공해야 한다.
3. 설정 파일 (`application.properties`, `application.yml`)
4. [RandomValuePropertySource](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/env/RandomValuePropertySource.html)
   랜덤값 설정 (`random.*`)
    - UUID, 포트 번호 등에서 사용되며 내부적으로 제공됨
5. 운영체제 환경 변수 (Environment Variables)
6. Java 시스템 프로퍼티 (`-Dkey=value` 형태)
7. JNDI 속성 (`java:comp/env`)
8. `ServletContext` 초기화 파라미터
9. `ServletConfig` 초기화 파라미터
10. `SPRING_APPLICATION_JSON` 환경변수 또는 시스템 프로퍼티
    - JSON 형식으로 설정값을 지정할 수 있음
    - `SPRING_APPLICATION_JSON='{"my":{"name":"test"}}' java -jar myapp.jar`
    - `java -Dspring.application.json='{"my":{"name":"test"}}' -jar myapp.jar`
    - `java -jar myapp.jar --spring.application.json='{"my":{"name":"test"}}'`
11. 명령줄 인자 (Command-line arguments)
    - 예: `--server.port=9090`
    - `SpringApplication.setAddCommandLineProperties(false)`를 통해 비활성화 할 수 있다.
12. 테스트에서 지정한 [
    `@SpringBootTest(properties = …)`](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#testing.spring-boot-applications.autoconfigured-tests)
13. 테스트 클래스 내 `@DynamicPropertySource`
14. `@TestPropertySource`
15. [DevTools 전역 설정](https://docs.spring.io/spring-boot/reference/using/devtools.html#using.devtools.globalsettings)
    - `$HOME/.config/spring-boot` 경로에 위치하며, DevTools가 활성화된 경우에만 적용됨

### 설정 파일(Config Data)의 로딩 우선순위

Spring Boot는 `application.properties` 및 `application.yml` 등의 설정 파일도 우선순위를 가지고 있으며, 아래와 같은 순서로 로딩된다.

1. JAR 내부에 포함된 `application.properties` 또는 `.yml`
2. JAR 내부에 포함된 프로파일 전용 파일
    - `application-{profile}.properties`
3. 외부 경로에 위치한 `application.properties` 또는 `.yml`
4. 외부 경로에 위치한 프로파일 전용 파일

> 참고: 동일한 경로에 `.properties`와 `.yml` 파일이 동시에 존재할 경우, `.properties` 파일이 우선 적용된다.

### 환경 변수와 키 네이밍 주의사항

운영체제 환경변수는 `.` 문자를 포함할 수 없으므로, 이를 사용할 때는 언더스코어(`_`)로 대체하여 지정해야 한다.

예를 들어, `spring.config.name` 대신 `SPRING_CONFIG_NAME`과 같은 형식을 사용한다.

---

## [외부 애플리케이션 속성](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files)

1. classpath
    - classpath root
    - classpath `/config` package
2. 현재 디렉토리에서
    - 현재 디렉토리
    - `config/` 현재 디랙토리의 하위 디렉토리
    - `config/` 하위 디렉토리의 직계 자식 디렉토리

### 클래스패스(classpath)와 파일 경로 prefix 정리

| Prefix        | 설명                                  |
|---------------|-------------------------------------|
| `classpath:`  | 현재 클래스패스를 기준으로 탐색하며, 테스트 리소스가 우선된다. |
| `classpath:/` | 클래스패스 루트 기준으로 절대 경로 탐색              |
| `file:`       | 절대 경로 또는 상대 파일 경로                   |
| `optional:`   | 파일이 없어도 예외를 발생시키지 않고 무시함            |
| `configtree:` | 파일 내용 자체가 설정 값으로 처리되는 특수 방식         |
| `jar:`        | JAR 내부 경로 지정 (복잡한 구조로 잘 사용되지 않음)    |

### 클래스패스 로딩 우선순위

| 우선순위             | 설명                |
|------------------|-------------------|
| `test/resources` | 테스트 리소스가 우선 탐색됨   |
| `main/resources` | 테스트 리소스 없을 때만 탐색됨 |

- 따라서 test에 `application.properties`가 있으면 main 설정을 덮어버림.
- 같은 이름을 덮어쓰려면 `import` 방식 대신 파일명이나 프로필로 구분하는게 낫다.

> test에서 `application.properties`를 정의하더라도 클래스패스 우선순위로 `import` 없이 그냥 두면, **main의 설정은 무시됨**

Spring은 `ClassLoader.getResources()` 를 사용하여 설정 파일을 로딩함. 이때 클래스패스의 우선순위는 다음과 같음

```
test/resoursce -> main/resourcee
```

- `classpath:`라고 했을 때 현재 **클래스패스 기준으로 탐색**.
- 즉, `test` 리소스 아래에서의 `classpath:`는 **`test/main/resources`부터 우선 탐색**
- 그리고 만약 같은 이름(`application.properties`)이 존재한다면, **main의 것은 무시함.**

예를 들어

```
src
  ㄴ main
     ㄴ resources
          ㄴ application.properties       <--  application-core 
  ㄴ test
     ㄴ resources
          ㄴ application.properties       <-- 테스트 전용
```

```
# test/main/resources/application.properties
spring.config.import=classpath:/application.properties
```

이 경우, 기대와 다르게 **main의 application.properties는 로딩되지 않음**

`classpaht:/`는 절대 경로처럼 보이지만 결국 **test/resources/application.properties** 를 먼저 로딩함.

이건 클래스패스 로딩 우선순위 때문에 어쩔 수 없음

- `src/test/resources` → 먼저 로딩
- `src/main/resources` → 나중 로딩
- **즉 같은 이름이면 test 리소스가 먼저 로딩됨**

여기서 `classpath:` 는 결국 자기 자신을 import하는 구조가 되고, 무한 루프는 발생하지 않지만 기대한 동작(메인 설정 상속)은 안 됨.

즉, 자기 자신을 import하게 되어, **test/resources/application.properties가 로딩됨. main 건 무시됨.**

### 그럼 `classpath:/`와 `classpath:`의 차이는 뭐냐?

이건 **상대경로냐, 절대경로냐** 차이만 있을뿐.

| 표현                                  | 의미                   |
|-------------------------------------|----------------------|
| `classpath:application.properties`  | 상대 경로 (현재 위치 기준)     |
| `classpath:/application.properties` | 클래스패스 루트 기준 절대 경로 탐색 |

- 테스트 실행 시 classpath는 **항상 test 리소스가 main 리소스보다 우선 탐색됨**.
- `classpath:/`로 main 리소스를 참조하고 싶어도 test에 같은 이름의 **main 리소스 파일은 무시됨**
- 즉 "**같은 파일명이 있으면 main/resources 파일은 아예 로딩되지 않는다**"가 본질

다음은 `spring.config.location`에 대한 공식 문서를 한국어로 자연스럽게 번역한 내용이다. 팀 또는 문서 공유 용도로 어투를 "\~다" 형태로 정리하였다.

---

## `spring.config.location`

Spring Boot는 `PropertySource`를 로딩할 때 특정한 우선순위 규칙을 따르며, 아래에 정의된 리스트의 **하위 항목일수록 상위 항목의 값을 덮어쓴다**.

불러온 설정 파일들은 `PropertySource` 인스턴스로 Spring `Environment`에 추가된다.

### 기본 파일명 변경하기

기본적으로 Spring Boot는 `application.properties` 또는 `application.yml`을 설정 파일로 사용하지만, `spring.config.name` 속성을 통해 다른 이름을 사용할 수
있다.

예를 들어, 설정 파일을 `myproject.properties` 또는 `myproject.yml`로 변경하고 싶다면 다음과 같이 실행하면 된다:

```shell
$ java -jar myproject.jar --spring.config.name=myproject
```

### 특정 경로 지정하기

`spring.config.location` 속성을 사용하면 명시적으로 설정 파일의 위치를 지정할 수 있다.
**여러 개의 경로를 지정할 수 있으며, 콤마(,)로 구분**한다.

예를 들어 다음과 같이 두 개의 파일을 지정할 수 있다:

```shell
$ java -jar myproject.jar --spring.config.location=\
optional:classpath:/default.properties,\
optional:classpath:/override.properties
```

`optional:` 접두어를 붙이면 해당 파일이 없어도 예외 없이 무시된다.

### 설정 시점 및 적용 우선순위

* `spring.config.name`, `spring.config.location`, `spring.config.additional-location` 세 가지 설정은 **애플리케이션이 시작되기 아주 초기에 평가
  **된다.
* 따라서 반드시 **OS 환경 변수**, **JVM 시스템 속성**, **커맨드라인 인자** 등으로 전달해야 한다.
* **application.properties 내부에서 설정해도 적용되지 않는다.**

### 디렉터리 경로 사용 시

`spring.config.location`에 디렉터리를 지정하는 경우, **슬래시(/)** 로 끝나야 한다.
이 경우 `spring.config.name` 값에 따라 자동으로 해당 디렉터리 내에서 설정 파일을 탐색한다.

```text
예: spring.config.location=classpath:/myconfig/
→ 내부적으로 classpath:/myconfig/application.properties, application-{profile}.properties 등을 탐색
```

지정된 파일 경로 또한 `-prod`, `-local` 등 **프로파일별 파일도 자동으로 인식**한다.

---

## 기본 경로와의 관계

`spring.config.location`을 설정하면, **기본 경로는 완전히 대체된다**.
즉, `classpath:/`, `classpath:/config/`, `file:./`, `file:./config/` 등은 무시되고, 명시된 경로만 읽히게 된다.

예를 들어 다음처럼 설정하면:

```shell
--spring.config.location=optional:classpath:/custom-config/,optional:file:./custom-config/
```

Spring Boot는 아래 두 경로만 탐색한다:

* `classpath:/custom-config/`
* `file:./custom-config/`

---

## 기본 경로는 유지하고, 추가로 덮어쓰기 하고 싶다면?

기본 위치를 유지하면서 설정 파일을 **추가로 병합**하고 싶다면 `spring.config.additional-location`을 사용한다.
이 옵션은 기존 기본 경로를 유지하면서 **추가 경로만 더 탐색**한다.

예시:

```shell
--spring.config.additional-location=optional:classpath:/custom-config/,optional:file:./custom-config/
```

이렇게 설정하면 최종적으로 다음 경로들이 순차적으로 탐색된다:

- 기본 경로들:
  -`optional:classpath:/`
  -`optional:classpath:/config/`
  -`optional:file:./`
  -`optional:file:./config/`
  -`optional:file:./config/*/`
- 추가 경로들:
  -`optional:classpath:/custom-config/`
  -`optional:file:./custom-config/`

* 공통 기본값은 `application.properties` 또는 지정한 이름의 설정 파일에 정의하고,
* 실제 운영, 테스트, 로컬 환경에서 필요한 값만 **추가 경로로 오버라이드** 하는 방식을 추천한다.

이렇게 하면 설정의 **기본값 → 재정의 → 우선순위**가 명확하게 구분되고,
설정 관리가 유연하면서도 명료해진다.

### `spring.config.location` vs `spring.config.import` 차이

| 항목                       | 설명                                                                                                                                                                           |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spring.config.location` | 초기 부트스트랩 단계에서 설정 파일을 로딩하기 위한 경로를 지정한다. 클래스패스 및 외부 파일 모두 지정 가능하며, `spring.config.import`보다 먼저 로딩된다.                                                                           |
| `spring.config.import`   | 설정 파일 로딩 체계를 재정의하며, 기본 병합 로직을 무효화한다. 명시적으로 지정된 설정만 로딩된다. <br/> 설정 파일 간 **병합(import)*- 용도, config server 연동에 필수 <br/>예: `spring.config.import=classpath:application-core.yml` |

`spring.config.import`를 사용하는 경우, 기본적으로 병합되던 main의 설정 파일(`application.properties`)은 더 이상 자동으로 로딩되지 않는다. 따라서 필요한 설정 파일은 모두
명시적으로 import 해야 한다.

- `spring.config.location`은 기본 classpath 설정 외에 추가 또는 대체 경로를 지정할 때 사용.
- 보통 `classpath:/application.properties`로 지정하면 기본 classpath 내 파일만 읽음.
- `spring.config.location`을 지정하면 기본 classpath 설정이 무시될 수 있으니 주의 필요.(기본 포함 안됨)

다음은 Spring Boot의 `Optional Locations`, `Wildcard Locations`, 그리고 `Profile Specific Files`에 대한 공식 문서를 팀 공유용으로 자연스럽게 번역한
내용입니다. 모든 문장은 `~다` 형태의 정중한 문서 어조로 작성되었다.

---

## Optional Locations (선택적 위치 지정)

기본적으로 Spring Boot는 지정한 설정 파일 경로가 존재하지 않으면 `ConfigDataLocationNotFoundException` 예외를 발생시키고 애플리케이션을 시작하지 않는다.

그러나 **설정 경로가 없어도 무시하고 실행을 계속하고 싶다면**, `optional:` 접두어를 사용할 수 있다. 이 접두어는 다음과 같은 프로퍼티에서 사용할 수 있다.

-`spring.config.location`
-`spring.config.additional-location`
-`spring.config.import`

예를 들어, 다음과 같이 설정하면:

```properties
spring.config.import=optional:file:./myconfig.properties
```

`myconfig.properties` 파일이 존재하지 않아도 애플리케이션은 정상적으로 시작된다.

또한, **모든 `ConfigDataLocationNotFoundException` 예외를 무시하고 실행을 계속하고자 할 경우**, `spring.config.on-not-found` 속성을 `ignore`로
설정하면 된다. 이 설정은 `SpringApplication.setDefaultProperties(...)`, 시스템 속성, 또는 환경 변수로 지정할 수 있다.

---

## Wildcard Locations (와일드카드 경로)

설정 파일 경로에 `*` 문자가 마지막 경로 세그먼트에 포함되어 있다면, 해당 경로는 **와일드카드 경로**로 인식된다.
Spring Boot는 설정을 로딩할 때 와일드카드를 확장하여 **하위 디렉토리까지 탐색**한다.

이 기능은 **Kubernetes 환경 등에서 Redis, MySQL 등 구성 파일을 분리 저장할 때 유용**하다.

예를 들어 다음과 같은 구조를 생각해보자:

```
/config/redis/application.properties  
/config/mysql/application.properties
```

이때 `spring.config.location=config/*/` 또는 `spring.config.additional-location=config/*/` 로 설정하면 두 파일 모두 읽힌다.

> 참고: Spring Boot는 기본적으로 `config/*/`를 외부 디렉터리의 기본 탐색 경로에 포함시킨다.

**사용 시 주의사항:**

- 와일드카드 경로는 **하나의 `*`만 포함되어야 하며**,
    - 디렉토리인 경우 `*/`로 끝나야 한다.
    - 파일인 경우 `*/파일명` 형식이어야 한다.
- **Classpath 경로에서는 와일드카드를 사용할 수 없다.**
- 경로는 **파일 이름의 절대 경로를 기준으로 알파벳 순으로 정렬**되어 처리된다.

---

## Profile Specific Files (프로파일별 설정 파일)

Spring Boot는 일반 설정 파일뿐 아니라, 다음과 같은 형식의 **프로파일별 설정 파일**도 자동으로 로딩한다.

```
application-{profile}.properties
application-{profile}.yml
```

예를 들어 `prod` 프로파일이 활성화되어 있고 YAML 형식을 사용한다면:

* `application.yml`
* `application-prod.yml`

두 파일 모두 로딩되며, `application-prod.yml`의 값이 우선 적용된다.

---

### 프로파일 우선순위 규칙

여러 프로파일이 활성화된 경우에는 **가장 마지막 프로파일이 우선순위를 가진다**.
예:

```properties
spring.profiles.active=prod,live
```

이 경우 `application-prod.properties`의 값은 `application-live.properties`에 의해 **덮어쓰기(overridden)** 된다.

단, 이 우선순위는 **로케이션 그룹 단위**로 적용된다.

#### 예시 1: 콤마(,) 구분 — 그룹별로 따로 처리됨

```properties
spring.config.location=classpath:/cfg/,classpath:/ext/
```

이 경우 `/cfg` 안의 파일을 모두 읽은 후, `/ext` 안의 파일을 읽는다. 즉:

```
/cfg/application-live.properties  
/ext/application-prod.properties  
/ext/application-live.properties
```

#### 예시 2: 세미콜론(;) 구분 — 그룹으로 묶여 동등 처리됨

```properties
spring.config.location=classpath:/cfg/;classpath:/ext/
```

이 경우 `/cfg`와 `/ext`를 동일 그룹으로 간주하므로, 다음과 같이 정렬 후 우선순위 적용:

```
/ext/application-prod.properties  
/cfg/application-live.properties  
/ext/application-live.properties
```

> `;`는 로케이션 그룹 단위를 정의하며, 같은 그룹 내에서는 프로파일 우선순위가 "가장 마지막 프로파일 기준"으로 처리된다.

### 기본 프로파일

Spring Boot는 별도 프로파일이 지정되지 않았을 경우, 자동으로 `[default]` 프로파일을 사용한다. 즉 `spring.profiles.active`가 지정되지 않으면
`application-default.properties`가 적용 대상이 된다.

---

### 중복 로딩 방지

하나의 프로파일별 설정 파일은 **한 번만 로딩된다**.

이미 직접 `application-prod.properties`를 명시적으로 import 했다면, 같은 파일이 다시 로딩되지 않는다.

---

이러한 규칙을 잘 이해하고 적용하면, 다양한 환경(로컬, 스테이징, 운영)에서 **신뢰 가능한 설정 계층과 오버라이딩 전략**을 설계할 수 있다.

---

## Spring Boot 설정 확장 개념 정리

Spring Boot는 다양한 설정 파일들을 효율적으로 관리하고 확장할 수 있도록 `spring.config.import`를 포함한 여러 메커니즘을 제공한다. 본 문서는 다음과 같은 주제를 중심으로 설명한다:

- `spring.config.import`의 동작 방식
- 고정 경로 vs 상대 경로
- 설정 우선순위
- 확장자 없는 파일 처리
- 환경 변수 및 구성 트리
- 속성 참조 (Property Placeholder)
- 다중 문서 처리
- 조건부 활성화
- 설정 값 암호화
- YAML 처리 방식

---

### 1. `spring.config.import`를 이용한 추가 설정 불러오기

`spring.config.import`를 사용하면 추가 설정 파일을 현재 파일 내에서 불러올 수 있다.

```properties
spring.application.name=myapp
spring.config.import=optional:file:./dev.properties
```

- 위 예시는 현재 디렉토리에 있는 `dev.properties`를 불러오며,
- 해당 파일이 존재하지 않아도 무시하고 실행이 가능하다.
- 불러온 설정 값은 현재 파일보다 우선 적용된다.
- 한 번 불러온 파일은 중복으로 불러오지 않는다.

여러 파일도 콤마로 구분하여 지정할 수 있으며, 나중에 불러온 파일이 우선순위를 갖는다.

---

### 2. 고정 경로 vs 상대 경로

* `/` 또는 `file:`, `classpath:` 같은 prefix가 있는 경우: **고정 경로**로 간주
* 나머지: 해당 import를 선언한 파일을 기준으로 하는 **상대 경로**로 간주

예:

```properties
spring.config.import=optional:core/core.properties
```

* 위 설정은 현재 설정 파일 기준 상대경로이며, `/demo/core/core.properties`가 로딩됨

---

### 3. 설정 우선순위

- `spring.config.import` 내에서 설정된 순서는 우선순위에 영향을 줌
- 나중에 정의된 설정이 먼저 정의된 설정을 덮어씀

```properties
spring.config.import=first.properties,second.properties
```

- `second.properties`의 설정이 `first.properties`의 값을 덮어쓸 수 있음

---

### 4. 확장자 없는 파일 로딩

클라우드 환경에서는 종종 확장자 없는 파일이 마운트됨. 이 경우 `[.확장자]` 힌트를 붙여서 Spring Boot가 파싱할 수 있도록 함.

```properties
spring.config.import=file:/etc/config/myconfig[.yaml]
```

---

### 5. 환경 변수에서 값 불러오기

멀티라인 환경 변수에 설정을 담고 `env:` 접두어로 import할 수 있음:

```properties
spring.config.import=env:MY_CONFIGURATION
```

* 기본 확장자는 `.properties`

---

### 6. Configuration Tree (Kubernetes ConfigMap/Secret 대응)

파일을 키, 내용물을 값으로 사용하는 디렉토리 구조를 속성으로 로딩 가능:

```properties
spring.config.import=optional:configtree:/etc/config/myapp
```

- `/etc/config/myapp/username` → `myapp.username` 속성
- `/etc/config/myapp/password` → `myapp.password` 속성

와일드카드도 가능:

```properties
spring.config.import=optional:configtree:/etc/config/*/
```

- 디렉토리 이름을 기반으로 속성 경로를 구성하며, 알파벳 순으로 로딩됨

---

### 7. 속성 참조 (Property Placeholder)

속성 값 안에서 `${key}` 형식으로 다른 속성을 참조할 수 있음:

```properties
app.name=MyApp
app.description=${app.name} is made by ${username:Unknown}
```

* `username` 값이 없으면 `Unknown`으로 대체됨
* 케밥 케이스(`my-prop`) 사용을 권장함

---

### 8. 다중 문서 처리

하나의 YAML 또는 Properties 파일 내에서 여러 설정 블록을 정의 가능:

#### YAML:

```yaml
spring:
  application:
    name: MyApp
---
spring:
  application:
    name: MyCloudApp
  config:
    activate:
      on-cloud-platform: kubernetes
```

#### Properties:

```properties
spring.application.name=MyApp
#---
spring.application.name=MyCloudApp
spring.config.activate.on-cloud-platform=kubernetes
```

* 각 문서는 독립적으로 처리되며, 아래 문서가 위 문서를 덮어쓸 수 있음

---

### 9. 조건부 활성화 (Activation Properties)

조건을 만족할 때만 특정 문서를 활성화할 수 있음:

```properties
spring.config.activate.on-profile=prod | staging
spring.config.activate.on-cloud-platform=kubernetes
```

* `spring.config.activate.on-profile`: 프로파일 일치 시 적용
* `spring.config.activate.on-cloud-platform`: 클라우드 플랫폼 감지 시 적용

---

### 10. 설정 값 암호화

Spring Boot 자체는 암호화 기능을 제공하지 않음. 그러나 `EnvironmentPostProcessor`를 통해 설정 값을 가공할 수 있다.

* 보안이 필요한 경우 Spring Cloud Vault 등을 사용하는 것이 바람직함

---

### 11. YAML 지원

Spring Boot는 SnakeYAML이 classpath에 있으면 YAML을 자동 지원한다. YAML은 계층 구조 데이터를 작성하기에 적합함.

#### 예시:

```yaml
environments:
  dev:
    url: https://dev.example.com
  prod:
    url: https://prod.example.com
```

→ 평탄화(flatten):

```properties
environments.dev.url=https://dev.example.com
environments.prod.url=https://prod.example.com
```

* 리스트는 인덱스를 사용하여 `my.list[0]`처럼 변환됨
* YAML 파일은 `@PropertySource`나 `@TestPropertySource`로는 로딩할 수 없음

---

이 문서는 Spring Boot 설정 파일의 유연한 구성과 다양한 환경 대응 방식을 설명하며, 특히 클라우드 환경에서의 운영에 유용한 정보를 포함하고 있다.

### 로컬 & 젠킨스 ST 빌드시 구성은 다음과 같다.

```
src/
 ㄴ main/
     ㄴ resources/
         ㄴ application.properties   <-- spring.profiles.active=prod
         ㄴ application-prod.properties  
                 ㄴ-- [공통] 모든 기존 프로퍼티 포함
                e.g) spring.config.import=classpath:application-core.properties
 ㄴ test/
     ㄴ resources/
         ㄴ application.properties <-- main prod 참조
         ㄴ application-local.properties
         ㄴ application-st-biuld.properties <-- test local 참조
```

### 기본: test/resource/application.properties

```
spring.config.import=classpath:application-prod.properties
#---
spring.profiles.active=@test.spring.profiles.active@ # Maven filtering
```

### 로컬: test/resource/application-local.properties

```
spring.config.import=classpath:application-prod.properties
#---
.... # 테스트에서 사용할 오버라이딩 설정

```

### ST Build: test/resource/application-st-buld.properties

```
spring.config.import=classpath:application-local.properties
# ^-- 테스트를 위해 정의한 프로퍼티를 그대로 상속받기 위함
#---
# ST 서버 빌드시 외부 서버와 의존된 프로퍼티 비활성화
redis.enabled=0
amqp.enabled=0
firebase.enabled=0
dynamodb.enabled=0
```

### 마무리

- Spring Boot 2.4+부터 spring.config.import 사용 시 기본 로딩 체계가 바뀌므로 주의
- **테스트와 메인 리소스**
    - **test/resources와 main/resources 동명의 파일은 "병합"이 아님*- → test/resources가 main/resources를 완전히 덮어씀(무시)
    - 테스트 프로퍼티가 main 설정을 덮는 문제는 클래스패스 우선순위 때문에 발생
- **spring.config.import**
    - "import"에 명시한 설정만 불러올 수 있으나  
      정확히는, 기존 기본 경로는 "import" 사용법/위치에 따라 "병합"될 수도 있음
      (import 자체가 "자동 병합을 끄는 것" 은 아님)
- **spring.config.location**
    - spring.config.location은 외부 설정 파일 위치 지정용, 남용 시 병합 로직 꼬일 수 있음
    - 지정을 하면 "기본 로딩 경로" (= classpath:/application.properties 등)을 더 이상 자동 탐색하지 않으므로, 기존 설정을 완전히 대체함
- **설정 우선순위**
    - Spring Cloud Config는
        - 커맨드라인/시스템 프로퍼티/환경변수 → (외부 config 파일, classpath config) → Config Server 순
        - 즉, config server property는 일반적으로 로컬파일보다 **높은 우선순위**로 적용
- **실행 시 / 병합/덮어쓰기 구분부터 명확히 할 것**
- Config Server는 label을 통해 버전 및 환경별 설정을 깔끔하게 분리 관리 가능
- Config Server가 native backend이면 searchLocations 설정값 확인 필수
- 운영 환경에서 JVM 옵션을 잘 설정해야 config 병합이 제대로 이루어짐

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
