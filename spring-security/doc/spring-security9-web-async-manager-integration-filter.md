# WebAsyncManagerIntegrationFilter

Async 웹 MVC를 지원하는 필터: WebAsyncManagerIntegrationFilter

스프링 MVC의 Async 기능(핸들러에서 Callable을 리턴할 수 있는 기능)을 사용할 때에도 SecurityContext를 공유하도록 도와주는 필터.

- PreProcess: SecurityContext를 설정한다.
- Callable: 비록 다른 쓰레드지만 그 안에서는 동일한 SecurityContext를 참조할 수 있다.
- PostProcess: SecurityContext를 정리(clean up)한다.

`WebAsyncManagerIntegrationFilter`는 Security Filter Chain 중 가장 먼저 실행되는 필터이다.

시큐리티 컨텍스트가 쓰레드 로컬을 사용한다. 즉 자기 자신과 동일한 쓰레드에서만 시큐리티 컨텍스트가 공유한다. 해당 필터는 Spring MVC Async Handler 를 지원하는 필터이다. 

Spring Async에서는 다른 쓰레드를 사용하고 있다. 즉 다른 쓰레드이지만, 동일한 컨텍스트를 사용할 수 있도록 제공해주는 필터이다.

# 스프링 시큐리티와 @Async
# @Async를 사용한 서비스를 호출하는 경우

- 쓰레드가 다르기 때문에 SecurityContext를 공유받지 못한다.

```java
SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
```

- SecurityContext를 자식 쓰레드에도 공유하는 전략.
- @Async를 처리하는 쓰레드에서도 SecurityContext를 공유받을 수 있다.

참고

- https://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html

기본 쓰레드 로컬이지만, 자식 쓰레드에도 공유하는 전략을 사용하여 해결 가능하다.

```java
SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
```
