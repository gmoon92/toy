## 8. IoC 컨테이너 6부: Environment 1부. 프로파일

프로파일과 프로퍼티를 다루는 인터페이스.

ApplicationContext 상속 받고 있는 EnvironmentCapable 에 대해 다룬다.
`EnvironmentCapable`는 제공하는 기능이 크게 2가지가 존재하는데 그중에 프로파일 기능에 대해 알아보자.

ApplicationContext extends EnvironmentCapable - getEnvironment()

```java
public interface ApplicationContext extends EnvironmentCapable, 
        ListableBeanFactory, 
        HierarchicalBeanFactory, 
        MessageSource, 
        ApplicationEventPublisher, 
        ResourcePatternResolver {
  
  // ...
}
```

### 프로파일

profile은 빈들의 묶음이다.

- 빈들의 그룹
- `Environment`의 역할은 활성화할 프로파일 확인 및 설정

각각의 환경에 따라 다른 빈들을 사용해야되는 경우 그 요구사항을 충족시키기 위해 Enviroment라는 인터페이스를 통해 사용할 수 있다.

Environment 는 EnvironmentCapable 를 통해 가져온다.

```text
# jvm option
-Dspring.profiles.active="test"
```

### 프로파일 유즈케이스

- 테스트 환경에서는 A라는 빈을 사용하고, 배포 환경에서는 B라는 빈을 쓰고 싶다.
- 이 빈은 모니터링 용도니까 테스트할 때는 필요가 없고 배포할 때만 등록이 되면 좋겠다.

### 프로파일 정의하기

- 클래스에 정의 
  - @Configuration @Profile(“test”) 
  - @Component @Profile(“test”)
- 메소드에 정의 
  - @Bean @Profile(“test”)

### 프로파일 설정하기

- -Dspring.profiles.avtive=”test,A,B,...”
- @ActiveProfiles (테스트용)

### 프로파일 표현식

- ! (not)
- & (and)
- | (or)

## 프로퍼티

- 다양한 방법으로 정의할 수 있는 설정값
- Environment의 역할은 프로퍼티 소스 설정 및 프로퍼티 값 가져오기

### 프로퍼티에는 우선 순위가 있다.

- StandardServletEnvironment의 우선순위 
  - ServletConfig 매개변수 
  - ServletContext 매개변수 
  - JNDI (java:comp/env/) 
  - JVM 시스템 프로퍼티 (-Dkey=”value”) 
  - JVM 시스템 환경 변수 (운영 체제 환경 변수)

### @PropertySource

- Environment를 통해 프로퍼티 추가하는 방법

### 스프링 부트의 외부 설정 참고

- 기본 프로퍼티 소스 지원 (application.properties)
- 프로파일까지 고려한 계층형 프로퍼티 우선 순위 제공