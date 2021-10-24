package com.gmoon.springasync.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@DisplayName("WorkQue가 full인 상태에서 task를 반환할 수 없거나," +
        "사용 가능한 스레드 또는 대기열 슬롯이 더 이상 없을 경우 발생할 수 있는 Executor 정책 테스트")
public class ThreadPoolExecutorPolicyTest {
  static final String THREAD_NAME_PREFIX = "thread-gmoon-pool";
  static final int MIN_CORE_SIZE = 1; // 최소 유지할 Thread 수
  static final int MAX_CORE_SIZE = 2; // 최대 Thread 수
  static final int POOL_KEEP_ALIVE_SECONDS = 60; // Thread 유지 시간
  static final int POOL_QUEUE_CAPACITY = 1; // Pool Work Que capacity...

  ThreadPoolTaskExecutor taskExecutor;
  int tryCount;

  @BeforeEach
  void setUp() {
    taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setThreadNamePrefix(THREAD_NAME_PREFIX);
    taskExecutor.setCorePoolSize(MIN_CORE_SIZE);
    taskExecutor.setMaxPoolSize(MAX_CORE_SIZE);
    taskExecutor.setKeepAliveSeconds(POOL_KEEP_ALIVE_SECONDS);
    taskExecutor.setQueueCapacity(POOL_QUEUE_CAPACITY);
    taskExecutor.setAllowCoreThreadTimeOut(false);

    tryCount = 100;
  }

  @Test
  @DisplayName("AbortPolicy: 기본 거부 정책은 이다. 작업 큐가 full이거나 모든 Thread가 busy인 경우. " +
          "즉 task를 생성할 수 없을 경우 RejectedExecutionException을 발생한다.")
  void abortPolicy() {
    // given
    setPolicy(new ThreadPoolExecutor.AbortPolicy());

    // when then
    while (tryCount-- > 0) {
      try {
        taskExecutor.execute(this::printThreadStatus);
      } catch (Exception e) {
        log.error("Not create new task. Because thread work que full...");
        assertThat(e).isInstanceOf(RejectedExecutionException.class);
      }
    }
  }

  @Test
  @DisplayName("CallerRunsPolicy: 메인 쓰레드가 종료되지 않았다면, 거부된 task는 메인 쓰레드에서 직접 실행한다.")
  void callerRunsPolicy() {
    // given
    setPolicy(new ThreadPoolExecutor.CallerRunsPolicy());

    // when then
    while (tryCount-- > 0) {
      taskExecutor.execute(this::printThreadStatus);
    }
  }

  @Test
  @DisplayName("DiscardPolicy: 거부된 task는 버려진다. Exception도 발생하지 않는다.")
  void discardPolicy() {
    // given
    setPolicy(new ThreadPoolExecutor.DiscardPolicy());

    // when then
    while (tryCount-- > 0) {
      taskExecutor.execute(this::printThreadStatus);
    }
  }

  @Test
  @DisplayName("DiscardOldestPolicy: 처리되지 않은 가장 오래된 요청을 버리고 execute을 재시도한다.")
  void discardOldestPolicy() {
    // given
    setPolicy(new ThreadPoolExecutor.DiscardOldestPolicy());

    // when then
    while (tryCount-- > 0) {
      taskExecutor.execute(this::printThreadStatus);
    }
  }

  private void setPolicy(RejectedExecutionHandler policy) {
    taskExecutor.setRejectedExecutionHandler(policy);
    taskExecutor.initialize();
    System.out.format("\nPolicy: %s\n", policy.getClass());
  }

  private void printThreadStatus() {
    int poolSize = taskExecutor.getPoolSize();
    int currentActiveThreadCount = taskExecutor.getActiveCount();
    String threadName = Thread.currentThread().getName();
    System.out.format("poolSize=%s, currentActiveThreadCount=%s [%s]\n", poolSize, currentActiveThreadCount, threadName);
  }
}
