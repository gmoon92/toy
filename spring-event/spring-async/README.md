## Spring Async

## 실행 방식

1. 송신자 이메일 설정
  - application.yml
    - `username: @mail.username@`
    - `password: @mail.password@`
2. 수신자 이메일 설정
  - `SampleDataRunner.java` 참고

## 학습 내용

스프링에서 비동기 처리를 @Async 어노테이션을 지원한다.
개발자는 비동기 처리를 하기 위해 @Async 어노테이션을 활용하면 된다.
비동기 처리는 스프링에게 맡기면 된다.

하지만 @Async 어노테이션의 오용으로 인해, 애플리케이션에 `memory leak`과 같은 성능 문제가 야기될 수 있다.
이외에도 비동기에서 `ThreadLocal`를 사용할 수 없는데, 이를 해결할 수 있는 방식에 대해서도 소개하려한다.

## 학습 목표

1. Spring async 동작 방식 이해
2. 사용법
4. @Async 에서 ThreadLocal 유지하기

## @EnableAsync 활성화

```java
@EnableAsync
@Configuration
public class SpringAsyncConfig {
}
```

- annotation: 커스텀 어노테이션 지정하여 사용 가능하다.
  - 기본적으로 Spring의 @Async 주석과 EJB 3.1 `javax.ejb.Asynchronous`를 감지한다.
- mode: 어드바이스 동작 방식을 정의한다.
  - AdviceMode.PROXY: JDK Proxy
  - AdviceMode.ASPECTJ: AspectJ Weaving 
- proxyTargetClass: 프록시 유형을 정의한다. `mode` 어노테이션 요소에서 `AdviceMode.PROXY` 지정해야만 지정된 옵션으로 정상 동작된다.
  - CGLIB: true
  - JDK Dynamic Proxy: false (default)
- order: `AsyncAnnotationBeanPostProcessor`가 적용되어야하는 순서를 설정한다. 기본적으로 모든 기존 프록시를 고려할 수 있도록 마지막으로 실행된다..

## 비동기 설정

사용법은 다음 두 가지 방법이 존재한다. 본 글에선 @Async 어노테이션을 활용할 예정이다.

- XML
- @Async Annotation

## @Async 어노테이션

@Async 어노테이션의 사용법은 간단하다.

```java
@Service
public class MemberService {

	@Async
	public void sendUserConfirmationMail() {
		// ...
	}
}
```

메일 발송과 같은 비동기로 동작해야할 메서드에 선언하면 된다.

하지만 2 가지 유의해야할 조건이 있다.

- It must be applied to public methods only.
- Self-invocation — calling the async method from within the same class — won't work.

스프링은 내부적으로 별도의 쓰레드를 생성하여 동작되기 때문에, 반환 타입은 기본적으로 void로 설정한다. 

### Future

비동기 처리된 결과를 받을 경우를 대비하여 `Future` 클래스를 사용하면 된다.

## 참고

- https://www.baeldung.com/spring-async
- https://www.baeldung.com/spring-enable-annotations
- https://www.baeldung.com/spring-security-async-principal-propagation
- https://www.baeldung.com/spring-mvc-async-security
