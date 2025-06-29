# Spring Boot 프로퍼티 설정

## [Profile Specific Files (프로파일별 설정 파일)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.profile-specific).  

Spring Boot는 기본 `application` 설정 파일 이외에도, `application-{profile}` 형식의 프로파일별 파일도 자동으로 로드한다.

예를 들어 `prod` 프로파일이 활성화되어 있고 YAML 형식을 사용한다면

- `application.yml`
- `application-prod.yml`

두 파일 모두 로딩되며, `application-prod.yml`의 값이 우선 적용된다.

프로파일별 설정 파일은 `application.properties`와 동일한 위치에서 탐색되고, 항상 공통 파일보다 더 높은 우선순위로 값을 덮어쓴다. 

### 프로파일 우선순위 규칙

여러 프로파일이 활성화된 경우에는 **가장 마지막 프로파일이 우선순위를 가진다**. (Last-wins 전략)

```properties
spring.profiles.active=prod,live
```

이 경우 `application-prod.properties`의 값은 `application-live.properties`에 의해 **덮어쓰기(overridden)** 된다.

이 Last-wins 전략은  **로케이션 그룹 단위**로 적용된다.

#### 예시 1: 콤마(,) 구분 — 그룹별로 따로 처리됨

```properties
spring.profiles.active=prod,live
spring.config.location=\
  classpath:/cfg/,\
  classpath:/ext/
```

이 경우 `classpath:/cfg/` 디렉터리 하위 파일들을 모두 읽은 후, `classpath:/ext/` 디렉터리의 파일들을 읽는다.

1. /cfg/application-live.properties
2. /ext/application-prod.properties
3. /ext/application-live.properties

#### 예시 2: 세미콜론(;) 구분 — 그룹으로 묶여 동등 처리됨

세미콜론(;)으로 구분하면 `/cfg`와 `/ext`를 같은 그룹으로 묶어, 그룹 내에서 Last-wins 규칙이 적용된다.  

```properties
spring.profiles.active=prod,live
spring.config.location=\
  classpath:/cfg/;\
  classpath:/ext/
```

이 경우 `/cfg`와 `/ext`를 **동일 그룹으로 간주하므로**, 다음과 같이 정렬 후 우선순위 적용되어 처리 순서가 다음과 같이 달라진다.

1. /ext/application-prod.properties  
2. /cfg/application-live.properties  
3. /ext/application-live.properties

> `;`는 로케이션 그룹 단위를 정의하며, 같은 그룹 내에서는 프로파일 우선순위가 "가장 마지막 프로파일 기준"으로 처리된다.

### 기본 프로파일

Spring Boot는 별도 프로파일이 지정되지 않았을 경우, 자동으로 `[default]` 프로파일을 사용한다. 즉 `spring.profiles.active`가 지정되지 않으면 `application-default.properties`가 적용 대상이 된다.

---

### 중복 로딩 방지

참고로, 모든 속성 파일(일반 및 프로파일별)은 한 번만 로드된다. 이미 직접 import한 경우, 같은 파일이 자동으로 중복 로드되지 않는다.

---

이러한 규칙을 잘 이해하고 적용하면, 다양한 환경(로컬, 스테이징, 운영)에서 **신뢰 가능한 설정 계층과 오버라이딩 전략**을 설계할 수 있다.

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
