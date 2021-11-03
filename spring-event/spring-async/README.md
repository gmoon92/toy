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

사용법은 다음 두 가지 방법이 존재한다. 본 포스팅에선 Java Config 방식을 소개하려한다.

- XML
- @Async Annotation

## @Async 어노테이션

Spring MVC Async 처리는 `@Async` 어노테이션을 사용하면 된다.
사용법은 간단하다. 메일 발송과 같은 비동기로 동작해야할 메서드에 `@Async` 어노테이션을 선언하면 된다.

```java

@Service
public class MailService {

  @Async
  public void sendInviteMailFrom(final String publicUrl) {
    // send mail logic...
  }
}
```

이때 @Async 어노테이션이 선언된 비동기 메서드는 다음 2 가지 조건을 충족해야만 비동기적으로 동작하게 된다.

- 반드시 public 접근제한자여야 한다.
- `Self-invocation`을 주의해야한다. 이는 Spring AOP 개념을 알고 있다면 당연한 결과이다.

> It must be applied to public methods only. <br/>
> Self-invocation — calling the async method from within the same class — won't
  work.

### 리턴 타입 비동기 메서드 

Spring은 내부적으로 `Async` 어노테이션이 선언된 메서드에 대해 최종적으로 `org.springframework.aop.interceptor.AsyncExecutionAspectSupport` 클래스에서 리턴 타입에 따라 다르게 동작하게 된다.

```java
public abstract class AsyncExecutionAspectSupport implements BeanFactoryAware {
  // ...
  
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

다음 `doSubmit` 메서드의 비동기 메서드 실행 방식을 살펴보면, void 메서드 외 3 가지의 리턴 타입에 따라 다르게 처리하고 있다는걸 알 수 있다.

- CompletableFuture
- ListenableFuture
- Future

따라서 비동기 처리된 결과를 받을 경우엔 다음 3 가지의 리턴 타입을 골라 사용하면 되는데, 참고로 `CompletableFuture`, `ListenableFuture` 클래스는 `java.util.concurrent.Future` 인터페이스를 구현한 클래스이다. 각각의 클래스별 동작 방식과 사용법이 다르다. 본 포스팅에선 해당 내용은 생략하겠다.

다음과 같이 `Future`를 구현한 `AsyncResult` 클래스를 사용하면 된다.

```java
@Service
public class MailService {

  @Async
  public Future<String> sendInviteMailFrom(final String publicUrl) {
    // send mail logic...
    return new AsyncResult<>(publicUrl);
  }
}

@Controller
public class MailController {
  @Autowried private MailService service;
  
  @PostMapping("/mail/send")
  public ResponseEntity<String> sendInviteMail() {
    String publicUrl = service.sendInviteMailFrom("https://gmoon92.github.io")
            .get();
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
```

## TaskExecutor, 독립적으로 실행한 가능한 작업기

Spring은 비동기 메서드를 실행하기 위해 `org.springframework.core.task.TaskExecutor`를 사용하여 실행하게 된다.

```java
package org.springframework.core.task;

@FunctionalInterface
public interface TaskExecutor extends java.util.concurrent.Executor {
  @Override
  void execute(Runnable task);
}
```

> `TaskExecutor`는 JDK 1.5의 Executor 인터페이스를 구현한 인터페이스로 기존 `Executor` 인터페이스의 기능과 완전히 같다. Spring 2.X 버전의 JDK 1.4 하위 호한성의 문제로 별도의 인터페이스로 분리되었다.<br/>
> 1. Spring PSA: Third party TaskExcutor에 대한 어댑터 역할 (Quartz , CommonJ WorkManager...) <br/>
> 2. Spring 자체적으로 확장 포인트를 주기 위함

기본적으로 Spring은 컨텍스트에서 `org.springframework.core.task.TaskExecutor` 빈
또는 `java.util.concurrent.Executor`를 사용하고 `taskExecutor` 빈 이름으로 등록된 Thread pool로
사용한다. 그렇지 않으면 메서드를 비동기적으로 실행하기 위해 `SimpleAsyncTaskExecutor`를 사용한다.

- org.springframework.core.task.TaskExecutor
- java.util.concurrent.Executor
- org.springframework.core.task.SimpleAsyncTaskExecutor

`SimpleAsyncTaskExecutor`는 Thread Pool이 아니므로 비동기 요청이 올때 마다 매번 Thread를 생성하게 된다. Thread 생성하는 비용이 크기 때문에 Overflow의 위험이 따른다.

> TaskExecutor implementation that fires up a new Thread for each task, executing it asynchronously.
Supports limiting concurrent threads through the "concurrencyLimit" bean property. By default, the number of concurrent threads is unlimited.<br/>
> NOTE: This implementation does not reuse threads! Consider a thread-pooling TaskExecutor implementation instead, in particular for executing a large number of short-lived tasks.<br/>
> - [javadoc-api: SimpleAsyncTaskExecutor.java](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/task/SimpleAsyncTaskExecutor.html)

`SimpleAsyncTaskExecutor`를 사용하고 있다면 Thread Pool를 구성하여 사용하도록 수정해야한다.

_**This implementation does not reuse threads!**_

## ThreadPoolTaskExecutor 설정

일반적으로 운영에선 `SimpleAsyncTaskExecutor`를 사용하지 않고 `TaskExecutor`를 재정의하여 사용한다.

`TaskExecutor`를 재정의 하기 위해선 `org.springframework.scheduling.annotation.AsyncConfigurer` 인터페이스를 구현한다.

`AsyncConfigurer` 인터페이스는 @EnableAsync 어노테이션이 달린 @Configuration 설정 클래스에 의해 구현될
Async 관련 설정 인터페이스이다. 이 인터페이스는 비동기 메서드 호출에서 사용할 `Executor`를 지정하고, 비동기
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
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
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
    - setRejectedExecutionHandler(): 거부 정책 설정 (default AbortPolicy)
    - initialize(): ExecutorService를 설정

참고로 Spring Boot 2.1 이상을 사용하고 있는 프로젝트라면, 기본적으로 `TaskExecutionAutoConfiguration`에서
`ThreadPoolTaskExecutor`을 빈으로 등록해준다. `application.yml` 설정은 다음과 같다.

```properties
spring:
  task:
    execution:
      thread-name-prefix: thread-gmoon-pool
      pool:
        core-size: 16
        max-size: 32
        keep-alive: 120
        queue-capacity: 500
        allow-core-thread-timeout: false
```

### workQueue(작업 큐) 종류

`queueCapacity` 설정에 따라 내부적으로 결정

- LinkedBlockingQueue: corePoolSize의 모든 Thread가 Busy 상태인 경우 새로운 테스크는 작업 큐에서 대기한다. 사실상 maximumPoolSize를 넘는 Thread는 생성되지 않기 때문에 이 값은 의미가 없다.
- SynchronousQueue:  Producer에서 생긴 작업을 Consumer인 Thread에 직접 전달한다. 사실상 큐가 아니며 Thread 간에 작업을 넘겨주는 역할만 한다. newCachedThreadPool()로 만들어진 객체의 작업 큐이다.

```java
public class ThreadPoolTaskExecutor extends ExecutorConfigurationSupport
        implements AsyncListenableTaskExecutor, SchedulingTaskExecutor {
  // ...
  
  protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
    BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);
    // ...
  }

  protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
    if (queueCapacity > 0) {
      return new LinkedBlockingQueue<>(queueCapacity);
    } else {
      return new SynchronousQueue<>();
    }
  }

}
```

### RejectedExecutionHandler

RejectedExecutionHandler는 ThreadPoolExecutor에서 task를 더 이상 받을 수 없을 때 호출된다. 
이런 경우는 큐 허용치를 초과하거나 Executor가 종료되어서 Thread 또는 큐 슬롯을 사용할 수 없는 경우에 발생한다.

### ThreadPoolExecutor Reject Policy

Executor는 작업 큐가 꽉 찼을 때 아래 4가지 전략 중에 하나를 선택해서 사용할 수 있다.

- AbortPolicy
  - Default로 설정되어 있는 정책이다. Reject된 task가 `RejectedExecutionException`을 던진다.
- CallerRunsPolicy
  - 메인 쓰레드가 종료되지 않았다면, 거부된 task는 메인 쓰레드에서 직접 실행한다.
- DiscardPolicy
  - 거부된 task는 무시된다. Exception도 발생하지 않는다.
- DiscardOldestPolicy
  - 실행자를 종료하지 않는 한 가장 오래된 처리되지 않은 요청을 삭제하고 execute()를 다시 시도한다.

### 비동기 메서드 별 Custom TaskExecutor 지정 

그외 별도로 @Async value 속성에 TaskExecutor 빈 이름을 지정 해주면 런타임 시 비동기 메서드는 지정된 TaskExecutor 빈을 사용하여 실행한다.

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

## AsyncUncaughtExceptionHandler, 예외 처리

반환 타입이 있는 비동기 메서드는 반환 값을 반환 받는 시점에 blocking 되기 때문에 예외 처리하기 쉽다.

하지만 리턴 타입이 없는 비동기 메서드일 경우엔 메인 쓰레드가 먼저 닫히는 경우가 있다. 
Spring에선 **리턴 타입이 없는 비동기 메서드**를 예외 처리를 할 수 있도록 `org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler` 인터페이스를 제공하고 있다.

```java
@Slf4j
public class CustomAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
  @Override
  public void handleUncaughtException(Throwable ex, Method method, Object... params) {
    log.info("Exception message - {}", ex.getMessage());
    log.info("Method name: {}", method.getName());
    for (Object param : params) {
      log.info("param: {}", param);
    }
  }
}
```

다음과 같이 getAsyncUncaughtExceptionHandler 메서드를 재정의하면 된다.

```java
@EnableAsync
@Configuration
public class SpringAsyncConfig implements AsyncConfigurer {
  // ...
  
  @Bean("asyncUncaughtExceptionHandler")
  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new CustomAsyncUncaughtExceptionHandler();
  }
}
```

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
- [https://github.com/eugenp/tutorials/blob/master/spring-scheduling/src/test/java/com/baeldung/async](https://github.com/eugenp/tutorials/blob/master/spring-scheduling/src/test/java/com/baeldung/async/AsyncAnnotationExampleIntegrationTest.java)
- [https://www.baeldung.com/spring-mvc-async-vs-webflux](https://www.baeldung.com/spring-mvc-async-vs-webflux)
