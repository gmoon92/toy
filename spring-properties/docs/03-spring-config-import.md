# Spring Boot 프로퍼티 설정

## [Spring Boot 설정 확장 개념 정리](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.importing)

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

## [추가 설정 데이터 import 하기: spring.config.import](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.importing)

`spring.config.import`를 사용하면 추가 설정 파일을 현재 파일 내에서 불러올 수 있다.

`import` 선언은 발견되는 즉시 처리되며, `import`를 선언한 파일 바로 아래 위치에 추가적인 설정 문서(document)로 삽입된다.

```properties
spring.application.name=myapp
spring.config.import=optional:file:./dev.properties
```

- 위 예시는 현재 디렉토리에 있는 `dev.properties`를 불러오며,
- 해당 파일이 존재하지 않아도 무시하고 실행이 가능하다.
- 불러온 설정 값은 현재 파일보다 우선 적용된다.
  - `dev.properties`에서 `spring.application.name`을 재정의하면 적용된다.
- 한 번 불러온 파일은 중복으로 불러오지 않는다.

여러 파일도 콤마로 구분하여 지정할 수 있으며, 나중에 불러온 파일이 우선순위를 갖는다.

> 참고: `spring.config.location` 은 초기 부트스트랩 단계에서 설정 파일을 로딩하기 위한 경로를 지정한다. 클래스패스 및 외부 파일 모두 지정 가능하며, `spring.config.import`보다 먼저 로딩된다. <br/>`spring.config.location`을 지정하면 기본 classpath 설정이 무시될 수 있으니 주의 필요.(기본 포함 안됨)

---

## [고정(Fixed) 경로 vs 상대(Import Relative) 경로](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.importing.fixed-and-relative-paths)

import 경로는 **고정 경로(Fixed)** 또는 **import 상대 경로(Import Relative)**로 지정할 수 있다.

고정 경로는 `spring.config.import`가 선언된 위치와 무관하게 항상 동일한 리소스를 참조한다. 상대 경로는 import를 선언한 파일의 위치를 기준으로 상대 경로로 해석된다.

- 고정 경로: `/` 또는 `file:`, `classpath:` 같은 URL 스타일 prefix로 시작하는 경우
- 상대 경로: 그 외의 경로는 import를 선언한 파일을 기준으로 **상대 경로**로 지정

> `optional:` 접두어는, 해당 경로가 고정인지 상대인지를 판별하는 데는 영향을 주지 않는다.

예를 들어

```text
/demo
    application.jar
    application.properties
    core/
        core.properties
        extra/
            extra.properties
```
```properties
spring.config.import=optional:core/core.properties
```

이 경우, 현재 설정 파일 기준 상대경로이며, `/demo/core/core.properties`가 로딩된다.

만약 `/demo/core/core.properties` 안에 다음과 같은 내용이 있다면:

```properties
spring.config.import=optional:extra/extra.properties
```

이 경우 상대 위치로 동작하므로, `/demo/core/extra/extra.properties` 파일이 불러와진다.

즉, `optional:extra/extra.properties`는 `/demo/core/core.properties`를 기준으로 상대 경로로 적용되어 결과적으로 `/demo/core/extra/extra.properties` 경로를 찾게 된다.

---

## [설정 우선순위](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.importing.import-property-order)

properties/yaml 파일에서, **한 문서 내에서 import가 정의되는 순서는 중요하지 않다.** 

예를 들어, 아래 두 예시는 같은 결과를 낸다.

```properties
spring.config.import=my.properties
my.property=value
```
```properties
my.property=value
spring.config.import=my.properties
```

- 위 두 예시 모두, import를 트리거한 파일보다, `spring.config.import`로 명시된 설정 파일이 우선순위가 높다.
- 여러 경로를 `spring.config.import` 키에 지정할 수 있다. 이 경우, 경로들은 나열된 순서대로 처리되며, 나중에 import된 파일이 우선순위가 높다.
    ```properties
    #`second`의 설정이 `first`의 값을 덮어쓸 수 있음
    spring.config.import=first.properties,second.properties
    ```
    - `second.properties`의 설정이 `first.properties`의 값을 덮어쓸 수 있음
- `spring.profiles.active`로 프로파일이 지정되어 있다면, `spring.config.import`에 명시된 설정 파일의 프로필별 설정 파일도 자동으로 함께 로드된다. (last win 전략)
    - 예를 들어, `my.properties`뿐 아니라 `my-<profile>.properties`도 자동으로 import된다.

> 우선순위가 높다는 건, 동일한 키의 값이 여러 군데 있을 때, 우선순위가 높은 파일의 값이 최종적으로 환경에 반영된다는 의미다.

그외 Spring Boot는 다양한 경로(위치)에서 설정 파일을 읽어올 수 있도록 확장 가능한 플러그형 API를 제공한다. 기본적으로 Java Properties, YAML, 구성 트리(configuration tree)에서 설정을 import할 수 있다.

필요하다면, 외부 라이브러리(jar)를 추가해서 더 다양한 방식으로 설정을 가져올 수도 있다.
이때, 가져올 파일이 반드시 서버 로컬에 존재할 필요는 없다. 예를 들어, Consul, Apache ZooKeeper, Netflix Archaius와 같은 외부 시스템(설정 저장소)에서 설정 데이터를 불러오도록 구현할 수도 있다.

만약 자신만의 특별한 위치(예: 새로운 저장소나 파일 시스템 등)에서 설정 파일을 읽어오고 싶다면, `org.springframework.boot.context.config` 패키지의 `ConfigDataLocationResolver` 와 `ConfigDataLoader` 클래스를 참조하여 직접 구현할 수 있다.

---

## [확장자 없는 파일 import 하기](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.importing-extensionless)

일부 클라우드 환경에서는 볼륨 마운트된 파일에 확장자를 추가할 수 없는 경우가 있다.

이런 확장자 없는(extensionless) 파일을 import 하려면, `[.확장자]` 힌트를 붙여서 Spring Boot가 어떤 방식으로 파일을 읽어야 하는지 알려줘야 한다.

```properties
spring.config.import=file:/etc/config/myconfig[.yaml]
```

---

## [환경 변수를 활용한 설정 값 관리](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.env-variables)

클라우드 플랫폼(예: Kubernetes)에서 애플리케이션을 실행할 때, 플랫폼이 제공하는 설정 값을 읽어야 하는 경우가 많다. 이런 값들은 환경 변수(environment variable)로 받아올 수도 있고, 설정 트리(configuration tree)를 사용할 수도 있다.

환경 변수에 전체 설정을 properties/yaml 형식의 멀티라인 값으로 저장해 둘 수도 있고, 이를 `env:` 프리픽스를 사용해 불러올 수 있다. 

예를 들어, 아래와 같이 `MY_CONFIGURATION`이라는 환경 변수에 여러 개의 키-값 쌍을 properties 파일 형태로 넣었다고 가정해보자.

```
my.name=Service1
my.cluster=Cluster1
```

`env:` 프리픽스를 사용하면, 이 환경 변수에 들어있는 모든 프로퍼티 값을 불러올 수 있다.

```properties
# 멀티라인 환경 변수에 설정 내용을 넣고, `env:` 접두어로 import할 수 있음
spring.config.import=env:MY_CONFIGURATION
```

이 기능은 확장자 지정도 지원하며, 기본 확장자는 `.properties`다.

---

## [Configuration Tree (Kubernetes ConfigMap/Secret 대응)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.configtree)

애플리케이션 설정 값을 환경 변수에만 저장하는 데에는 한계가 있다. 특히 비밀번호나 인증키처럼 민감한 정보라면 더욱 보안에 신경 써야 한다.

이런 이유로, 최근 클라우드 환경에서는 설정을 파일이나 볼륨처럼 별도의 저장소에 마운트해서 쓰는 방식을 많이 활용한다. 예를 들어 `Kubernetes`에서는 `ConfigMap`이나 `Secret`을 볼륨으로 마운트할 수 있다.

이 방식은 보통 두 가지 패턴으로 쓰인다.

1. 전체 설정 내용을 하나의 파일(YAML 등)에 담아서 마운트하는 방식
2. 여러 파일로 나눠 디렉터리 구조로 저장하고, 파일 이름이 키, 파일 내용이 값이 되게 하는 형식

첫 번째라면 기존처럼 `spring.config.import`로 파일을 직접 불러오면 된다.

### 디렉토리 구조를 속성으로 바로 로딩 (configtree:)

두 번째처럼 여러 파일을 디렉터리 트리로 쪼개 저장하는 경우에는 `configtree:` 접두어를 사용해야 스프링이 각 파일을 별도 설정 값으로 읽어온다.

예를 들어, Kubernetes에서 다음과 같이 볼륨을 마운트했다고 하자.


```
etc/
  config/
    myapp/
      username    # 사용자명(키)
      password    # 비밀번호(키)
```

각각의 파일에 실제 설정 값이 들어있다.

이럴 때는 `application.properties`나 `application.yaml`에서 다음처럼 설정하면 끝이다.

```properties
spring.config.import=optional:configtree:/etc/config/
```
- `/etc/config/myapp/username` → `myapp.username` 프로퍼티
- `/etc/config/myapp/password` → `myapp.password` 프로퍼티

**폴더 구조와 파일 이름이 그대로 환경 프로퍼티 네이밍에 반영된다.** 만약 username과 password를 접두어 없이 바로 쓰고 싶다면 경로를 `/etc/config/myapp`으로 바꿔주면 된다.

점(`.`)이 들어간 파일명도 무리 없이 잘 매핑된다. (예를 들어, `/etc/config/myapp.username` ⇒ `myapp.username` 프로퍼티로 인식)

설정 트리의 값은 자동으로 문자열(String) 또는 바이너리(byte[])로 바인딩된다.

### 와일드카드로 여러 디렉토리 한번에 불러오기

여러 설정 디렉터리를 한 번에 import 하고 싶을 때는 와일드카드(*) 문법도 쓸 수 있다.

예를 들어 다음 구조를 보자.

```
etc/
  config/
    dbconfig/
      db/
        username
        password
    mqconfig/
      mq/
        username
        password
```

아래와 같이 import 하면 모든 설정이 한 번에 환경에 등록된다.

```properties
spring.config.import=optional:configtree:/etc/config/*/
```

이렇게 하면 db.username, db.password, mq.username, mq.password 프로퍼티가 모두 활성화된다.

> 와일드카드 *로 여러 디렉토리를 불러올 경우, 디렉터리 이름 기준으로 알파벳 순서대로 프로퍼티가 등록된다. 만약 설정이 겹치는 부분이 있으면, 순서상 나중에 불러온 쪽이 우선 적용된다. 확정된 로딩 순서가 필요하다면 각각의 경로를 개별적으로 import에 나열하면 된다.

이 같은 방식은 Docker secrets에도 똑같이 쓸 수 있다.

예를 들어 Docker Swarm에서 db.password란 시크릿 파일이 `/run/secrets/`에 마운트되어 있다면 아래처럼 import하면 바로 쓸 수 있다.

```properties
spring.config.import=optional:configtree:/run/secrets/
```

이처럼 설정 트리를 이용하면 좀 더 보안적으로 안전하게, 그리고 유연하게 애플리케이션 설정을 관리할 수 있다.

---

## [속성 참조 (Property Placeholder)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.property-placeholders)

속성 값 안에서 `${key}` 형식으로 다른 속성을 참조할 수 있다.

`${이름:기본값}`처럼 콜론(`:`) 뒤에 기본값을 넣으면, 해당 설정이 없을 때 기본값이 대신 사용된다.

```properties
app.name=MyApp
app.description=${app.name} is made by ${username:Unknown}
```

- `username` 값이 없으면 `Unknown`으로 대체됨
- 케밥 케이스(`my-prop`) 사용을 권장함

플레이스홀더에서 참조할 때는 반드시 스프링 표준 규칙(모두 소문자, 케밥케이스: item-price)을 쓰는 게 좋다. 그래야 다양한 타입의 환경 변수, 시스템 프로퍼티 등에서도 값을 자동으로 매칭해준다.

예를 들어 `${demo.item-price}`라고 하면,
- `demo.item-price`, `demo.itemPrice` (카멜케이스), `DEMO_ITEMPRICE` (대문자/언더스코어 환경 변수)
  까지도 자동 인식해 찾아준다.

반대로 `${demo.itemPrice}`라고 쓸 경우,
- `demo.item-price`나 `DEMO_ITEMPRICE`은 매칭되지 않는다.

### 기존 프로퍼티의 "단축형" 만들기

이런 플레이스홀더 문법을 활용하면, 기존의 긴 스프링부트 프로퍼티 키 대신 “짧은 별칭”도 쉽게 만들 수 있다.  

자세한 내용은 [How-to Guide - 단축 커맨드라인 옵션 사용법](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.args.short-command-line-arguments)을 참고하면 된다.

---

## [다중 문서 처리](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.multi-document)

Spring Boot에서는 한 개의 설정 파일(physical file) 안에 여러 개의 논리적 도큐먼트(document)를 분리해서 쓸 수 있다.

각 도큐먼트는 독립적으로 적용되며, 위에서 아래 순서대로 처리된다. 아래쪽 도큐먼트가 위쪽에서 정의한 값을 덮어쓸 수 있다는 점이 특징이다.

멀티 도큐먼트 설정 파일은 보통 `spring.config.activate.on-profile` 같은 활성화 조건과 함께 조합해서 많이 쓴다. (예: 프로파일, 클라우드 환경에 따라 설정 분기)

> 참고: 멀티 도큐먼트 형식의 프로퍼티 파일은 `@PropertySource`나 `@TestPropertySource` 어노테이션으로는 불러올 수 없다.

### application.yaml : YAML 멀티 도큐먼트

YAML 파일에서는 표준 문법인 연속된 하이픈 `---` 세 개로 각 도큐먼트를 구분한다.

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

### application.properties : 주석 기반 멀티 도큐먼트

properties 파일에서는 `#---` 또는 `!---`와 같은 주석(comment) 라인으로 도큐먼트를 구분할 수 있다.

```properties
spring.application.name=MyApp
#---
spring.application.name=MyCloudApp
spring.config.activate.on-cloud-platform=kubernetes
```

여기서 구분선은 꼭 **공백 없이** `#---` 또는 `!---`로만 작성해야 하고, 구분선의 앞뒤에는 같은 종류의 주석이 바로 이어지면 안 된다.

---

## [조건부 활성화 (Activation Properties)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.activation-properties)

특정 조건이 충족될 때만 일부 설정 속성을 적용해야 할 때가 있다. 예를 들어, 어떤 속성들은 **특정 프로파일이 활성화될 때만 의미가 있을 수 있다.**

이런 경우, `spring.config.activate.*`을 사용해서 설정 파일을 조건부로 활성화할 수 있다. 사용할 수 있는 주요 활성화 속성은 다음과 같다.

| 속성                | 설명                                                              |
|-------------------|-----------------------------------------------------------------|
| on-profile        | 해당 설정이 활성화되기 위한 프로필 표현식 또는 여러 프로필 중 하나라도 해당될 때 활성화하도록 지정할 수 있다. |
| on-cloud-platform | 해당 설정이 활성화되기 위해 탐지되어야 하는 클라우드 플랫폼을 지정한다.                        |

예를 들어, 아래와 같이 설정하면 두 번째 설정 문서는 쿠버네티스 환경에서 실행되고, 프로필이 'prod' 또는 'staging'일 때에만 활성화된다.

```properties
myprop=always-set
#---
spring.config.activate.on-profile=prod | staging
spring.config.activate.on-cloud-platform=kubernetes
```

* `spring.config.activate.on-profile`: 프로파일 일치 시 적용
* `spring.config.activate.on-cloud-platform`: 클라우드 플랫폼 감지 시 적용

**YAML 예시**

```yaml
myprop: always-set
---
spring:
  config:
    activate:
      on-cloud-platform: kubernetes
      on-profile: prod | staging
myotherprop: sometimes-set
```

Spring Boot에서는 `on-profile`의 경우 "!" 연산자를 사용해서 부정할 수 있다.

```properties
# https://www.baeldung.com/spring-profiles
spring.config.activate.on-profile=prod & !staging
```

반면에 `on-cloud-platform`은 공식 문서상에서는 아직 부정(!) 연산자를 지원하지 않는다.

---

## [설정 값 암호화](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.encrypting)

Spring Boot 자체는 암호화 기능을 제공하지 않음. 그러나 `EnvironmentPostProcessor`를 통해 설정 값을 가공할 수 있다.

- 보안이 필요한 경우 Spring Cloud Vault 등을 사용하는 것이 바람직함

---

## [YAML 지원](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.yaml)

YAML은 JSON의 상위 호환이기 때문에, 계층적인 설정 정보를 표현할 때 매우 편리하다.  

Spring Boot에서는 클래스패스에 SnakeYAML 라이브러리가 있으면 `SpringApplication`이 `properties` 파일 대신 YAML 파일도 자동으로 지원한다.

> `spring-boot-starter`에 SnakeYAML이 이미 포함되어 있어 별도 설치 없이 사용할 수 있다.

#### YAML과 Properties 매핑

YAML 문서는 계층적인 구조를 가지므로, 이를 Spring의 Environment에서 쓸 수 있도록 평평한(Flat) 구조로 바꿔야 한다.  
예를 들어, 다음과 같은 YAML이 있다고 하자.

```yaml
environments:
  dev:
    url: "https://dev.example.com"
    name: "Developer Setup"
  prod:
    url: "https://another.example.com"
    name: "My Cool App"
```

이 내용을 환경(Environment)에서 사용하려면 아래와 같이 평탄화된다.

```
environments.dev.url=https://dev.example.com
environments.dev.name=Developer Setup
environments.prod.url=https://another.example.com
environments.prod.name=My Cool App
```

마찬가지로, YAML에서 리스트를 사용할 경우 `[index]` 표기법을 통해 속성으로 변환된다.  
예를 들어 다음과 같다.

```yaml
my:
  servers:
    - "dev.example.com"
    - "another.example.com"
```

위와 같은 리스트는 아래처럼 변환된다.

```
my.servers[0]=dev.example.com
my.servers[1]=another.example.com
```

이와 같이 `[index]` 형식의 프로퍼티는 Spring Boot의 Binder 클래스를 통해 Java의 `List`나 `Set` 객체에 바인딩할 수 있다. (자세한 내용은 타입 세이프 설정(Type-safe Configuration Properties) 문서를 참고)

참고로, `@PropertySource`나 `@TestPropertySource` 애노테이션으로는 YAML 파일을 불러올 수 없다.  
이런 방식을 꼭 사용해야 한다면 properties 파일을 써야 한다.

#### YAML 직접 로딩하기

Spring Framework에서는 YAML 문서를 직접 불러올 수 있도록 편리한 클래스들을 제공한다.
- `YamlPropertiesFactoryBean` : YAML을 `Properties` 객체로 변환해서 로딩
- `YamlMapFactoryBean` : YAML을 `Map` 형태로 변환해서 로딩

또한, YAML 파일을 Spring의 `PropertySource`로 직접 불러오고 싶다면 `YamlPropertySourceLoader` 클래스를 사용하면 된다.

---

## [랜덤 값 주입 (RandomValuePropertySource)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.random-values)

`random` 프로퍼티 소스를 통해 보안 값, 테스트 데이터 등에 사용할 수 있는 랜덤 값을 프로퍼티로 주입할 수 있다.

```properties
my.secret=${random.value}
my.number=${random.int}
my.bignumber=${random.long}
my.uuid=${random.uuid}
my.number-less-than-ten=${random.int(10)}
my.number-in-range=${random.int[1024,65536]}
```

- `${random.int(10)}` → 0 이상 10 미만 정수
- `${random.int[1024,65536]}` → 1024 이상 65536 미만 정수

> `random.int(*)` 문법은 구간을 설정하는 데 사용하며, `(min,max)` 형태에서 min과 max는 정수값이다. `()` 외에도 `[]`, `{}` 등의 기호도 지원한다.

---

## [시스템 환경 변수 prefix 설정](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.system-environment)

여러 Spring Boot 애플리케이션이 하나의 시스템 환경 변수 공간을 공유할 경우, 충돌 방지를 위해 prefix를 설정할 수 있다.

```java
SpringApplication app = new SpringApplication(MyApp.class);
app.

setEnvironmentPrefix("INPUT");
```

위 설정 이후, `remote.timeout` 이라는 속성은 시스템 환경 변수에서는 `INPUT_REMOTE_TIMEOUT` 으로 인식된다. 

**이 접두사는 `시스템 환경 변수`에만 적용된다.**

즉, 시스템 환경이 아닌 다른 설정 소스에서는 여전히 `remote.timeout`이란 이름을 그대로 사용한다.

---

이 문서는 Spring Boot 설정 파일의 유연한 구성과 다양한 환경 대응 방식을 설명하며, 특히 클라우드 환경에서의 운영에 유용한 정보를 포함하고 있다.

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
