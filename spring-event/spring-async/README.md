## Spring Async

## 실행 방식

1. 송신자 이메일 설정

- application.yml
    - `username: @mail.username@`
    - `password: @mail.password@`

2. 수신자 이메일 설정

- `SampleDataRunner.java` 참고

## 학습 내용

스프링에서 비동기 처리를 @Async 어노테이션을 지원한다. 개발자는 비동기 처리를 하기 위해 @Async 어노테이션을 활용하면 된다. 비동기
처리는 스프링에게 맡기면 된다.

하지만 @Async 어노테이션의 오용으로 인해, 애플리케이션에 `memory leak`과 같은 성능 문제가 야기될 수 있다. 이외에도
비동기에서 `ThreadLocal`를 사용할 수 없는데, 이를 해결할 수 있는 방식에 대해서도 소개하려한다.

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
- proxyTargetClass: 프록시 유형을 정의한다. `mode` 어노테이션 요소에서 `AdviceMode.PROXY` 지정해야만 지정된
  옵션으로 정상 동작된다.
    - CGLIB: true
    - JDK Dynamic Proxy: false (default)
- order: `AsyncAnnotationBeanPostProcessor`가 적용되어야하는 순서를 설정한다. 기본적으로 모든 기존 프록시를
  고려할 수 있도록 마지막으로 실행된다..

## 비동기 설정

사용법은 다음 두 가지 방법이 존재한다. 본 글에선 @Async 어노테이션을 활용할 예정이다.

- XML
- @Async Annotation

## @Async 어노테이션

@Async 어노테이션의 사용법은 간단하다.

```java

@Service
public class MailService {

  @Async
  public void sendInviteMailFrom(final String publicUrl) {
    // send mail logic...
  }
}
```

메일 발송과 같은 비동기로 동작해야할 메서드에 선언하면 된다.

하지만 2 가지 유의해야할 조건이 있다.

- It must be applied to public methods only.
- Self-invocation — calling the async method from within the same class — won't
  work.

스프링은 내부적으로 별도의 쓰레드를 생성하여 동작되기 때문에, 반환 타입은 기본적으로 void로 설정한다.

### 리턴 타입 비동기 메서드 

Spring에선 비동기 메서드의 동작 방식은 `org.springframework.aop.interceptor.AsyncExecutionAspectSupport` 클래스를 살펴보면 된다.

- CompletableFuture
- ListenableFuture
- Future

```java
public abstract class AsyncExecutionAspectSupport implements BeanFactoryAware {
  
  protected Object doSubmit(Callable<Object> task, AsyncTaskExecutor executor, Class<?> returnType) {
    if (CompletableFuture.class.isAssignableFrom(returnType)) {
      return CompletableFuture.supplyAsync(() -> {
        try {
          return task.call();
        } catch (Throwable ex) {
          throw new CompletionException(ex);
        }
      }, executor);
    } else if (ListenableFuture.class.isAssignableFrom(returnType)) {
      return ( (AsyncListenableTaskExecutor) executor ).submitListenable(task);
    } else if (Future.class.isAssignableFrom(returnType)) {
      return executor.submit(task);
    } else {
      executor.submit(task);
      return null;
    }
  }
}
```

만약 비동기 처리된 결과를 받을 경우엔 `java.util.concurrent.Future` 인터페이스를 사용하면 된다.

```java

@Service
public class MailService {

  @Async
  public Future<String> sendInviteMailFrom(final String publicUrl) {
    // send mail logic...
    return new AsyncResult<>(publicUrl);
  }
}
```

반환 값은 `Future`를 구현한 `AsyncResult` 클래스를 사용하면 된다.

## TaskExecutor

기본적으로 Spring은 컨텍스트에서 `org.springframework.core.task.TaskExecutor` 빈
또는 `java.util.concurrent.Executor`를 사용하고 `taskExecutor` 빈 이름으로 등록된 Thread pool로
사용한다. 그렇지 않으면 메서드를 비동기적으로 실행하기 위해 `SimpleAsyncTaskExecutor`를 사용한다.

- org.springframework.core.task.TaskExecutor
- java.util.concurrent.Executor
- org.springframework.core.task.SimpleAsyncTaskExecutor

주의 사항으로 `TaskExecutor`로 `SimpleAsyncTaskExecutor`를 사용하고 있다면 주의해야한다.
`SimpleAsyncTaskExecutor`는 Thread Pool이 아니다. 따라서 비동기 메서드를 실행할 때마다 매번 Thread를
생성하는데, Thread 생성하는 비용이 크기 때문에 Overflow의 위험이 있다.

- This implementation does not reuse threads!

```text
TaskExecutor implementation that fires up a new Thread for each task, executing it asynchronously.
Supports limiting concurrent threads through the "concurrencyLimit" bean property. By default, the number of concurrent threads is unlimited.

NOTE: This implementation does not reuse threads! Consider a thread-pooling TaskExecutor implementation instead, in particular for executing a large number of short-lived tasks.
```

[javadoc-api: SimpleAsyncTaskExecutor.java](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/task/SimpleAsyncTaskExecutor.html)

일반적으로 운영에선 `SimpleAsyncTaskExecutor`를 사용하지 않고 `TaskExecutor`를 재정의하여 사용한다.

## TaskExecutor 설정

먼저 `org.springframework.scheduling.annotation.AsyncConfigurer` 인터페이스를 구현한다.

`AsyncConfigurer` 인터페이스는 @EnableAsync 어노테이션이 달린 @Configuration 설정 클래스에 의해 구현될
Async 관련 설정 인터페이스이다. 이 인터페이스는 비동기 메서드 호출에서 사용할 `Executor`를 지정하고, void 반환 타입 비동기
메서드의 예외 처리를 지원하는 `AsyncUncaughtExceptionHandler` 클래스 설정을 도와준다.

우선 TaskExecutor 설정을 살펴보자.

```java

@EnableAsync
@Configuration
public class SpringAsyncConfig implements AsyncConfigurer {
  private static final String THREAD_NAME_PREFIX = "thread-gmoon-pool";
  private static final int DEFAULT_CORE_SIZE = 16;
  private static final int MAX_CORE_SIZE = 32;
  private static final int POOL_KEEP_ALIVE_SECONDS = 120;
  private static final int POOL_QUEUE_CAPACITY = 500;

  @Bean("taskExecutor")
  @Primary
  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setThreadNamePrefix(THREAD_NAME_PREFIX);
    taskExecutor.setCorePoolSize(DEFAULT_CORE_SIZE);
    taskExecutor.setMaxPoolSize(MAX_CORE_SIZE);
    taskExecutor.setKeepAliveSeconds(POOL_KEEP_ALIVE_SECONDS);
    taskExecutor.setQueueCapacity(POOL_QUEUE_CAPACITY);
    taskExecutor.setAllowCoreThreadTimeOut(false);
    taskExecutor.initialize();
    return taskExecutor;
  }
}
```

- [ThreadPoolTaskExecutor](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/concurrent/ThreadPoolTaskExecutor.html)
  는 Java Bean 스타일로 구성되어 있고 설정 방식도 빈 스타일로 구성하면 된다. 또한, Thread 관리 및 모니터링(ex. JMX)을
  지원한다.
    - 런타임 시 업데이트 지원 속성: `corePoolSize`, `maxPoolSize`, `keepAliveSeconds`
    - 자체 검사 전용 속성: `poolSize`, `activeCount`
    - setThreadNamePrefix(): 새로 생성된 스레드의 이름에 사용할 접두사를 지정 (default beanName + "
      -")
    - setCorePoolSize(): 코어 풀 크기 설정
    - setMaxPoolSize(): 최대 풀 크기 설정
    - setKeepAliveSeconds(): 연결 유지 초를 설정 (default 60)
    - setQueueCapacity(): `BlockingQueue`에 대한 용량 설정 (default Integer.MAX_VALUE)
    - setAllowCoreThreadTimeOut(): 코어 스레드의 시간 초과를 허용할지 여부 (default false)
    - initialize(): ExecutorService를 설정

참고로 Spring Boot 2.1 이상을 사용하고 있는 프로젝트라면, 기본적으로 `TaskExecutionAutoConfiguration`에서
등록되어진 Executor 빈이 없다면 `ThreadPoolTaskExecutor`을 등록하게 된다. `application.yml` 설정은
다음과 같다.

```properties
spring:
task:
execution:
thread-name-prefix:thread-gmoon-pool
pool:
core-size:16
max-size:32
keep-alive:120
queue-capacity:500
allow-core-thread-timeout:false
```

그외 별도로 특정 TaskExecutor 빈을 통해 비동기 메서드를 실행할 경우라면 다음 처럼 @Async value 속성에
TaskExecutor 빈 이름을 지정 해주면, 런타임 시 비동기 메서드는 지정된 TaskExecutor 빈을 사용하여 실행한다.

```java

@Service
public class MailService {
  @Async("customTaskExecutor") // 커스텀한 TaskExecutor bean name 지정
  public void sendInviteMailFrom(final String publicUrl) {
    // send mail...
    return new AsyncResult<>(publicUrl);
  }
}
```

## 예외 처리

반환 타입이 있는(`Future`) 비동기 메서드는 예외 처리를 하기 쉽다.

### Async 관련 클래스

- AsyncExecutionInterceptor
- AsyncAnnotationBeanPostProcessor
    - Spring 5.1 부터 추가된 @Async 어노테이션 관련 `BeanPostProcessor` 이다. Executor 와
      Exception handler 를 해당 BeanPostProcessor를 통해 설정된다.
    - Configure this post-processor with the given executor and exception
      handler suppliers, applying the corresponding default if a supplier is not
      resolvable. Since:5.1
- AsyncExecutionAspectSupport
- AnnotationAsyncExecutionInterceptor
    - 해당 인터셉터는 우리가 일반적으로 알고 있는 MVC 패턴에서 `DispatcherServlet`이 컨트롤러를 실행하기 전에 실행되는
      인터셉터가 아닌, Adivce를 주입하여 모델링하기 위한 클래스 스프링만의 명명 규칙이다.
    - Advice: Many AOP frameworks, including Spring, model an advice as an
      interceptor, maintaining a chain of interceptors “around” the joinpoint.
- AsyncAnnotationAdvisor
    - Async 관련 어드바이저
    - advice: AnnotationAsyncExecutionInterceptor

```java
public class AsyncExecutionInterceptor extends AsyncExecutionAspectSupport implements MethodInterceptor, Ordered {

  public Object invoke(final MethodInvocation invocation) throws Throwable {
    // async advice logic...
  }
}
```

## 참고

- [https://www.baeldung.com/spring-async](https://www.baeldung.com/spring-async)
- [https://www.baeldung.com/spring-enable-annotations](https://www.baeldung.com/spring-enable-annotations)
- [https://www.baeldung.com/spring-security-async-principal-propagation](https://www.baeldung.com/spring-security-async-principal-propagation)
- [https://www.baeldung.com/spring-mvc-async-security](https://www.baeldung.com/spring-mvc-async-security)
- [https://github.com/eugenp/tutorials/blob/master/spring-scheduling/src/test/java/com/baeldung/async](https://github.com/eugenp/tutorials/blob/master/spring-scheduling/src/test/java/com/baeldung/async/AsyncAnnotationExampleIntegrationTest.java
- [https://www.baeldung.com/spring-mvc-async-vs-webflux](https://www.baeldung.com/spring-mvc-async-vs-webflux)