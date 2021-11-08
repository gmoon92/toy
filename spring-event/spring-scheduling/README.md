# Spring Scheduling

본 문서는 Spring Scheduler 에 대해 학습하려 한다.

Spring은 3.0 부터 `org.springframework.scheduling.TaskScheduler`를 통해 스케쥴러 기능을 지원한다.

> 스케쥴러란 특정한 시간에 원하는 일(Job)을 자동으로 수행하는 일련의 작업을 뜻한다.

본 문서는 Spring이 지원하는 `TaskScheduler`에 대해 다룬다.

- 스케쥴러 job 생성 등록
- 스케쥴러 예약 정책

본 문서에서 다루는 모든 코드는 [GitHub](https://github.com/gmoon92/Toy/tree/master/spring-event/spring-scheduling) 을 참고하자.

## TaskScheduler

`TaskScheduler` 인터페이스는 다음과 같다.

```java
package org.springframework.scheduling;

public interface TaskScheduler {
  ScheduledFuture<?> schedule(Runnable task, Trigger trigger);
  ScheduledFuture<?> schedule(Runnable task, Date startTime);
  ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period);
  ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period);
  ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay);
  ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay);
}
```

모든 메서드는 작업을 수행할 `Runnable` 인터페이스를 첫 번째 인자로 받고, 두 번째 인자들은 작업이 수행할 시간을 받고 있다는걸 알 수 있다.

좀 더 정확한 확인을 위해 `org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler` 클래스를 통해 `TaskScheduler` 학습 테스트를 진행하겠다.

## ThreadPoolTaskScheduler

`ThreadPoolTaskScheduler` 클래스는 TaskExecutor 인터페이스를 구현한 클래스로, 스케쥴에 관련된 모든 작업 실행을 Thread Pool을 지원하는 `ScheduledExecutorService`에 위임한다. 
효과적인 메모리 관리뿐만 아니라, 단일 인스턴스가 비동기로 실행될 수 있도록 지원한다.

우선 다음과 같이 `ThreadPoolTaskScheduler`를 빈으로 등록하자.

```java
@Configuration
public class ThreadPoolTaskSchedulerConfig {
  private static final String NAME_OF_THREAD_SCHEDULER_TASK = "scheduler-thread-gmoon-pool";
  private static final int SIZE_OF_POOL = 5;

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setThreadNamePrefix(NAME_OF_THREAD_SCHEDULER_TASK);
    taskScheduler.setPoolSize(SIZE_OF_POOL);
    taskScheduler.initialize();
    return taskScheduler;
  }
}
```

다음으로 테스트 코드에서 `ThreadPoolTaskScheduler`에 스케쥴러를 등록하여 작업이 정상적으로 실행되는지 확인해본다.

- `taskScheduler.schedule(task, startTime);`

```java
@Slf4j
@SpringBootTest
class ThreadPoolTaskSchedulerConfigTest {
  @Autowired ThreadPoolTaskScheduler taskScheduler;

  @Test
  @DisplayName("스케쥴러는 1초뒤에 예약된 작업을 실행한다.")
  void schedule() throws InterruptedException {
    // given 
    Runnable task = Mockito.spy(() -> log.info("test"));
    
    // when
    executeTaskOnSchedulerAfterOneSeconds(task);
    Thread.sleep(2_000);

    // then
    then(task).should(times(3)).run();
  }

  private void executeTaskOnSchedulerAfterOneSeconds(Runnable task) {
    Date startTime = new Date(System.currentTimeMillis() + 1_000);
    taskScheduler.schedule(task, startTime);
  }
}
```

테스트 결과는 통과된다. 스케쥴러에 등록된 1초마다 작업을 실행한다.

이외에도 cron 표현식을 지원하며, 단 하나의 인스턴스만 실행하는 `FixedDelay` 방식, 이외에도 이전 작업과 상관 없이 일정 시간이 되면 작업을 실행하는 `FixedRate` 방식을 지원한다. 그외 다양한 형식을 지원하는데 [GitHub 학습 테스트 코드](https://github.com/gmoon92/Toy/blob/master/spring-event/spring-scheduling/src/test/java/com/gmoon/springscheduling/config/ThreadPoolTaskSchedulerConfigTest.java) 를 참고하자.

## @Scheduled Annotation

@Scheduled 어노테이션을 사용하여, 위처럼 코드 구현 없이 스케쥴러에 작업을 등록할 수 있다.  

````java
@Configuration
@EnableScheduling
public class ThreadPoolTaskSchedulerConfig {
  // ...
}
````

@EnableScheduling 어노테이션을 설정 클래스 파일에 선언하여 @Scheduled 어노테이션을 활성해줘야 한다.

@Scheduled 어노테이션은 메서드에 선언하며, 스케쥴러에 등록하기 위해선 다음 두 가지 메서드 조건이 필요하다.

- void return type method
- 인자가 없는 무항 메서드

```java
@Slf4j
@Service
public class SimpleScheduledJobs {
  private static final String FORMAT_OF_TASK_MESSAGE = "schedule tasks using {} jobs - {}";

  @Scheduled(fixedDelay = 1_000)
  public void task() {
    log.info(FORMAT_OF_TASK_MESSAGE, "task", getCurrentSeconds());
  }
}
```

### @Scheduled with @Async

@Scheduled 어노테이션과 @Async 어노테이션을 같이 사용하면 스케쥴러를 비동기 처리를 할 수 있다.

```java
@Service
public class SimpleScheduledJobs {
  @Async
  @Scheduled(fixedDelay = 1_000)
  public void task() {
    // logic...
  }
}
```

### Fixed Rate vs Fixed Delay

- fixedDelay: 이전 작업이 끝난 후, **이전 작업이 끝난 시간 기준으로** 지정된 시간 이후에 다음 작업을 수행한다.
- fixedRate: 이전 작업과 상관 없이, **이전 작업이 시작된 시간 기준으로** 지정된 시간마다 작업이 수행한다.
  - memory leak이 발생할 수 있다.
- cron: cron 표현식을 지원한다.

## Runtime Dynamic Trigger Config

@Scheduled 어노테이션은 Spring 컨텍스트 시작 시 한 번만 초기화된다. 따라서 Spring에서 런타임에 fixedDelay 또는 fixedRate 주기를 변경할 수 없다.

Spring에선 `org.springframework.scheduling.Trigger` 인터페이스를 사용하여 런타임에 동적으로 주기를 설정할 수 있다.

우선 SchedulingConfigurer 인터페이스를 구현 해보자

```java
@Configuration
@EnableScheduling
public class DynamicSchedulingConfig implements SchedulingConfigurer {

  private final PhoneAlarmJobs alarmJobs;
  private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

  @Override
  public void configureTasks(ScheduledTaskRegistrar registrar) {
    registrar.setScheduler(threadPoolTaskScheduler);
    registrar.addTriggerTask(alarmJobs::wakeUp, getDynamicDelayTimeTrigger());
  }

  private Trigger getDynamicDelayTimeTrigger() {
    return context -> {
      Optional<Date> lastCompletionTime = Optional.ofNullable(context.lastCompletionTime());
      Instant nextExecutionTime = lastCompletionTime.orElseGet(Date::new)
              .toInstant()
              .plusMillis(alarmJobs.getPlusSecondsDelay());
      return Date.from(nextExecutionTime);
    };
  }
}

@Slf4j
@Service
public class PhoneAlarmJobs {
  private static final int MILLISECONDS_OF_PLUS_TIME = 500;

  private long delay = 0;

  @Async
  public void wakeUp() {
    final long now = System.currentTimeMillis() / 1000;
    log.info("schedule phone alarm tasks with dynamic delay - {}", now);
  }

  public long getPlusSecondsDelay() {
    this.delay += MILLISECONDS_OF_PLUS_TIME;
    log.info("delaying {} milliseconds...", this.delay);
    return this.delay;
  }
}
```

## Custom Exception Handler

스케쥴러 예외를 처리하는 방식은 크게 두 가지다.

- org.springframework.util.ErrorHandler
- org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler

### ErrorHandler

ThreadPoolTaskScheduler 빈에 `ErrorHandler` 인터페이스를 구현한 클래스를 등록시켜주면 된다. 

```java
@Slf4j
public class CustomErrorSchedulerHandler implements ErrorHandler {

  @Override
  public void handleError(Throwable t) {
    log.error("Exception in scheduler task.", t);
  }
}

@Configuration
@EnableScheduling
public class ThreadPoolTaskSchedulerConfig{
  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setThreadNamePrefix(NAME_OF_THREAD_SCHEDULER_TASK);
    taskScheduler.setPoolSize(SIZE_OF_POOL);
    taskScheduler.setErrorHandler(new CustomErrorSchedulerHandler()); // 에러 핸들러 등록
    taskScheduler.initialize();
    return taskScheduler;
  }
}
```

### AsyncUncaughtExceptionHandler

스케쥴러를 비동기로 실행되고 있다면, AsyncUncaughtExceptionHandler 인터페이스를 구현한 클래스를 통해 예외를 처리할 수 있다.

```java
@Slf4j
public class CustomAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

  @Override
  public void handleUncaughtException(Throwable ex, Method method, Object... params) {
    log.error("async uncaught exception....", ex);
  }
}
```

다음으로 ThreadPoolTaskSchedulerConfig 설정 파일에 `AsyncConfigurer` 인터페이스를 구현하고, `getAsyncUncaughtExceptionHandler` 메서드를 재정의하여 커스텀으로 구현한 핸들러를 반환해주면 된다.

```java
@Configuration
@EnableAsync
@EnableScheduling
public class ThreadPoolTaskSchedulerConfig implements AsyncConfigurer {
  
  // ...

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new CustomAsyncUncaughtExceptionHandler();
  }
}
```

본 문서에서 다루는 모든 코드는 [GitHub](https://github.com/gmoon92/Toy/tree/master/spring-event/spring-scheduling) 을 참고하자.

## 참고

- [Spring integration docs - scheduling](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling)
- [A Guide to the Spring Task Scheduler](https://www.baeldung.com/spring-task-scheduler)
- [The @Scheduled Annotation in Spring](https://www.baeldung.com/spring-scheduled-tasks)
- [Guide to Spring Retry](https://www.baeldung.com/spring-retry)
- [How To Do @Async in Spring](https://www.baeldung.com/spring-async)
- [Conditionally Enable Scheduled Jobs in Spring](https://www.baeldung.com/spring-scheduled-enabled-conditionally)
- [Remote Debugging with IntelliJ IDEA](https://www.baeldung.com/intellij-remote-debugging)

