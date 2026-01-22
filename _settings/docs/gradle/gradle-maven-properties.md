# Gradle로 빌드 툴 변경 시 Maven 프로퍼티 치환 이슈 및 해결 방안

## 사례

기존에는 Maven 멀티모듈 프로젝트에서

`application.properties` 또는 `application.yml` 파일 안에  
`@property@` 또는 `${property}` 형태로 Maven의 프로퍼티 값을 치환해서 사용했음.

예를 들어

```yaml
appstore:
  enabled: @appstore.enabled@
```

이런 식의 값을 Maven 빌드시 `<properties>`나 profile에서 정의한 대로 바꿔서 배포했음.

프로젝트를 Gradle로 전환하면서, 동일한 문법(`@property@`, `${property}`)이 더 이상 빌드 시 치환되지 않고,  
plain 텍스트 그대로 남거나 YAML 파서에서 에러가 발생하는 문제가 생겼음.

## Maven과 Gradle의 차이

- Maven은 리소스 필터링 기능이 내장되어 있어, `${prop}` 또는 `@prop@` 문자를 사용하면  
  빌드시 값을 자동 치환해줌. `<properties>`, profile 등과 연동이 쉬움.
- Gradle은 기본적으로 리소스 필터링을 하지 않음.  
  특별한 설정 없이 placeholder 문자가 원본 그대로 전달됨.
- Gradle에서 Maven처럼 프로퍼티를 치환하려면 빌드 스크립트에서 별도 설정이 필요함.

## Gradle에서 빌드시 프로퍼티 치환 방법

Gradle에서는 `processResources` 태스크에 `expand()` 기능을 이용해야 함.  
`expand()`는 빌드시 Gradle의 변수나 프로젝트 프로퍼티 값을 파일 내 플레이스홀더와 매칭하여 치환해줌.

예시)

```groovy
processResources {
    filesMatching('application.yml') {
        expand(project.properties)
    }
}
```

- 위 코드에서, `application.yml`에 `@prop@`가 있으면,  
  Gradle의 `-Pprop=value` 또는 `gradle.properties`에 정의된 값으로 치환 가능

또는 특정 값만 매핑해서 치환하고 싶으면

```groovy
processResources {
    filesMatching('application.yml') {
        expand(appstoreEnabled: project.findProperty('appstore.enabled') ?: 'false')
    }
}
```

빌드 명령어:

``` shell
./gradlew build -Pappstore.enabled=true
```

## 정리

- Maven의 리소스 필터링 단계와 Gradle의 기본 동작이 다르므로  
  기존에 application.properties/application.yml에서 profile이나 properties 값을 빌드시 치환해서 쓰던 방식이 동작하지 않음.
- Gradle에서는 processResources에서 filesMatching, expand등을 활용해서 명시적으로 값 치환 설정을 해야 동작함.
- 이 방법으로 Maven에서 사용하던 profile 분기나 프로퍼티 기반 분기도 Gradle에서 동일하게 설정할 수 있음.
