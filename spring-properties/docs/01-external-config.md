# Spring Boot 프로퍼티 설정

## [외부 애플리케이션 속성](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files)

Spring Boot는 애플리케이션이 시작될 때 아래 위치에서 자동으로 `application.properties` 및 `application.yaml` 파일을 찾아서 로드한다.

1. classpath 기준
    - 클래스패스 루트(root)
    - 클래스패스의 `/config` 패키지
2. 현재 디렉토리 기준
    - 현재 디렉터리
    - 현재 디렉터리의 config/ 하위 디렉터리
    - config/ 하위의 직계 자식 디렉터리

Spring Boot는 `PropertySource`를 로딩할 때 특정한 우선순위 규칙을 따르며, 아래에 정의된 리스트의 **하위 항목일수록 상위 항목의 값을 덮어쓴다**.

불러온 설정 파일들은 `PropertySource` 인스턴스로 Spring `Environment`에 추가된다.

### 클래스패스(classpath)와 파일 경로 prefix 정리

| Prefix        | 설명                                              |
|---------------|-------------------------------------------------|
| `classpath:`  | 현재 클래스패스를 기준으로 탐색하며, 테스트 리소스가 우선된다.             |
| `classpath:/` | 클래스패스 루트 기준으로 절대 경로 탐색                          |
| `file:`       | 파일 시스템 경로 (절대/상대 파일 경로)                         |
| `optional:`   | 파일이 없어도 예외를 발생시키지 않고 무시함                        |
| `configtree:` | 파일 내용 자체가 설정 값으로 처리되는 특수 방식 (Kubernetes 등에서 사용) |
| `jar:`        | JAR 내부 경로 지정 (복잡한 구조로 잘 사용되지 않음)                |

- `classpath:`는 테스트 실행 시 `test/resources`가 우선 탐색되므로, 같은 파일명이면 `test/resources`가 main 보다 우선 로드되어 main 파일은 가려짐.
- `classpath:/`와 `classpath:` 차이는 절대경로냐 상대경로냐 차이지만, 결국 클래스패스 우선순위 규칙은 동일.

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

---

## 기본 파일명 변경하기: `spring.config.name`

기본적으로 Spring Boot는 `application.properties` 또는 `application.yml`을 설정 파일로 사용하지만, `spring.config.name` 속성을 통해 다른 이름을 사용할 수 있다.

예를 들어, 설정 파일을 `myproject.properties` 또는 `myproject.yml`로 변경하고 싶다면 다음과 같이 실행하면 된다:

```shell
$ java -jar myproject.jar --spring.config.name=myproject
```

---

## 특정 경로 지정하기: `spring.config.location`

`spring.config.location` 속성을 사용하면 명시적으로 설정 파일의 위치를 지정할 수 있다. **여러 개의 경로를 지정할 수 있으며, 콤마(,)로 구분**한다.

예를 들어 다음과 같이 두 개의 파일을 지정할 수 있다.

```shell
$ java -jar myproject.jar --spring.config.location=\
optional:classpath:/default.properties,\
optional:classpath:/override.properties
```

`optional:` 접두어를 붙이면 해당 파일이 없어도 예외 없이 무시된다.

### 설정 시점 및 적용 우선순위

`spring.config.name`, `spring.config.location`, `spring.config.additional-location` 세 가지 설정은 **애플리케이션이 시작되기 아주 초기 단계에서 어떤 파일을 불러올지 결정하는 데 사용된다.**

따라서, 이 속성들은 반드시 **OS 환경 변수**, **JVM 시스템 속성**, 또는 **커맨드라인 인자** 등으로 전달해야 한다.

**application.properties 내부에서 설정해도 적용되지 않는다.**

### 디렉터리 경로 사용 시

`spring.config.location`에 디렉터리를 지정하는 경우, **슬래시(/)** 로 끝나야 한다. 런타임에는 `spring.config.name`에 의해 생성된 파일명이 해당 디렉터리 경로 뒤에 붙어서 파일이 탐색된다.

`spring.config.location`에 지정한 파일들은 직접적으로 `import`된다. 디렉터리와 파일 경로 모두 프로필별 파일도 함께 탐색하여 로드한다.

예를 들어, `spring.config.location=classpath:/myconfig/`로 지정되어 있다면, 내부적으로 `classpath:myconfig-<profile>.properties` 파일도 함께 로드된다.

```properties
spring.config.location=\
  classpath:/myconfig/application.properties,\
  application-common.properties,\
  application-prod.properties,
```

지정된 파일 경로 또한 `-prod`, `-local` 등 **프로파일별 파일도 자동으로 인식한다.**

### 기본 경로와의 관계

`spring.config.location`을 설정하면, **기본 경로는 완전히 대체된다**.

즉, `classpath:/`, `classpath:/config/`, `file:./`, `file:./config/` 등은 무시되고, 명시된 경로만 읽히게 된다.

예를 들어 다음처럼 설정하면, Spring Boot는 아래 두 경로만 탐색한다.

```properties
spring.config.location=\
  optional:classpath:/custom-config/,\
  optional:file:./custom-config/
```

1. `classpath:/custom-config/`
2. `file:./custom-config/`

---

## 기본 경로는 유지하고, 추가로 덮어쓰기 하고 싶다면?: spring.config.additional-location

기본 위치를 유지하면서 설정 파일을 **추가로 병합**하고 싶다면 `spring.config.additional-location`을 사용한다. 추가된 경로에서 불러온 설정값은, 기본 위치의 값보다 우선적으로 적용되어 덮어쓸 수 있다.

이 옵션은 기존 기본 경로를 유지하면서 **추가 경로만 더 탐색**한다.

```properties
spring.config.additional-location=\
  optional:classpath:/custom-config/,\
  optional:file:./custom-config/
```

이렇게 설정하면 최종적으로 다음 경로들이 순차적으로 탐색된다.

- 기본 경로들
  -`optional:classpath:/`
  -`optional:classpath:/config/`
  -`optional:file:./`
  -`optional:file:./config/`
  -`optional:file:./config/*/`
- 추가 경로들
  -`optional:classpath:/custom-config/`
  -`optional:file:./custom-config/`

공통 기본값은 `application.properties` 또는 지정한 이름의 설정 파일에 정의하고, 실제 운영, 테스트, 로컬 환경에서 필요한 값만 **추가 경로로 오버라이드** 하는 방식을 추천한다.

이렇게 하면 설정의 **기본값 → 재정의 → 우선순위**가 명확하게 구분되고, 설정 관리가 유연하면서도 명료해진다.

---

## [Optional Locations (선택적 위치 지정)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.optional-prefix)

기본적으로 Spring Boot는 지정한 설정 파일 경로(config data location)가 존재하지 않으면, `ConfigDataLocationNotFoundException` 예외를 발생시키고 애플리케이션을 시작되지 않는다.

그러나 **설정 경로가 없어도 무시하고 실행을 계속하고 싶다면**, `optional:` 접두어를 사용할 수 있다. 이 접두어는 다음과 같은 프로퍼티에서 사용할 수 있다.

-`spring.config.location`
-`spring.config.additional-location`
-`spring.config.import`

예를 들어, 다음과 같이 설정하면 `myconfig.properties` 파일이 존재하지 않아도 애플리케이션은 정상적으로 시작된다.

```properties
spring.config.import=optional:file:./myconfig.properties
```

만약, **모든 `ConfigDataLocationNotFoundException` 예외를 무시하고 실행을 계속하고자 할 경우**, `spring.config.on-not-found` 속성을 `ignore`로 설정하면 된다. 

이 설정은 `SpringApplication.setDefaultProperties(...)`, 시스템 속성, 또는 환경 변수로 지정할 수 있다.

---

## [Wildcard Locations (와일드카드 경로)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.wildcard-locations)

설정 파일 경로의 마지막 부분에 `*` 문자가 포함되어 있으면, 해당 경로는 **와일드카드 위치로 간주된다.** Spring Boot는 설정을 로딩할 때 와일드카드를 확장하여 **하위 디렉토리까지 탐색**한다.

이 기능은 **Kubernetes 환경 등에서 Redis, MySQL 등 구성 파일을 분리 저장할 때 유용**하다.

예를 들어 다음과 같은 구조를 생각해보자.

```
/config/redis/application.properties  
/config/mysql/application.properties
```

이때 `spring.config.location=config/*/` 또는 `spring.config.additional-location=config/*/` 로 설정하면 두 파일 모두 읽힌다.

> 참고: Spring Boot는 기본적으로 `config/*/`를 외부 디렉터리의 기본 탐색 경로에 포함시킨다.

### 사용 시 주의사항

- 와일드카드 경로는 **반드시 `*` 하나만 포함해야 한다.**
    - 디렉토리인 경우 `*/`로 끝나야 한다.
    - 파일인 경우 `*/<파일명>` 형식이어야 한다.
- 와일드카드가 포함된 경로들은 **파일 이름의 절대 경로를 기준으로 알파벳 순으로 정렬**되어 처리된다.
- **와일드카드 경로는 외부 디렉터리에서만 동작한다.**
  - `classpath:` 로 시작하는 클래스패스 경로에서는 와일드카드를 사용할 수 없다.

---

이러한 규칙을 잘 이해하고 적용하면, 다양한 환경(로컬, 스테이징, 운영)에서 **신뢰 가능한 설정 계층과 오버라이딩 전략**을 설계할 수 있다.

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
