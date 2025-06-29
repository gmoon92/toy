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

- `spring.config.import` 내에서 설정된 순서는 우선순위에 영향을 줌
- 나중에 정의된 설정이 먼저 정의된 설정을 덮어씀

```properties
spring.config.import=first.properties,second.properties
```

- `second.properties`의 설정이 `first.properties`의 값을 덮어쓸 수 있음

---

### 확장자 없는 파일 로딩

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

다음은 `Spring Boot` 설정에서 다루는 **랜덤 값 설정**, **시스템 환경 변수 설정**, 그리고 **타입 세이프 설정 바인딩(@ConfigurationProperties)** 에 대한 정리 내용이다.
공유용 문서로도 쓸 수 있도록 `~다` 어투로 작성하였다.

---

## Spring Boot 설정 고급 사용법 정리

### 랜덤 값 주입 (RandomValuePropertySource)

`random` 프로퍼티 소스를 통해 보안 값, 테스트 데이터 등에 사용할 수 있는 랜덤 값을 프로퍼티로 주입할 수 있다.

```properties
my.secret=${random.value}
my.number=${random.int}
my.bignumber=${random.long}
my.uuid=${random.uuid}
my.number-less-than-ten=${random.int(10)}
my.number-in-range=${random.int[1024,65536]}
```

* `${random.int(10)}` → 0 이상 10 미만 정수
* `${random.int[1024,65536]}` → 1024 이상 65536 미만 정수

> `random.int(*)` 문법은 구간을 설정하는 데 사용하며, `(min,max)` 형태에서 min과 max는 정수값이다. `()` 외에도 `[]`, `{}` 등의 기호도 지원한다.

---

### 시스템 환경 변수 prefix 설정

여러 Spring Boot 애플리케이션이 하나의 시스템 환경 변수 공간을 공유할 경우, 충돌 방지를 위해 prefix를 설정할 수 있다.

```java
SpringApplication app = new SpringApplication(MyApp.class);
app.

setEnvironmentPrefix("INPUT");
```

위 설정 이후, `remote.timeout` 이라는 속성은 시스템 환경 변수에서는 `INPUT_REMOTE_TIMEOUT` 으로 인식된다. 다만, 다른 설정 소스에서는 여전히 `remote.timeout` 으로 사용된다.

---

이 문서는 Spring Boot 설정 파일의 유연한 구성과 다양한 환경 대응 방식을 설명하며, 특히 클라우드 환경에서의 운영에 유용한 정보를 포함하고 있다.

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
