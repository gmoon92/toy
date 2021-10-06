## Spring Async 

스프링에서 비동기 처리를 @Async 어노테이션을 지원한다.
개발자는 비동기 처리를 하기 위해 @Async 어노테이션을 활용하면 된다.
비동기 처리는 스프링에게 맡기면 된다.

하지만 @Async 어노테이션의 오용으로 인해, 애플리케이션에 `memory leak`과 같은  치명적인 성능 문제로 야기될 수 있다.

## 학습 목표

1. Spring async 동작 방식 이해
2. 사용법
4. @Async 에서 ThreadLocal 유지하기


