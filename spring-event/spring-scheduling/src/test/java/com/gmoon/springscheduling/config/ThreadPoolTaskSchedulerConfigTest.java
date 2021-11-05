package com.gmoon.springscheduling.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Date;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

@Slf4j
@SpringBootTest
class ThreadPoolTaskSchedulerConfigTest {

  @Autowired
  ThreadPoolTaskScheduler taskScheduler;

  @Test
  @DisplayName("스케쥴러는 1초뒤에 예약된 작업을 실행한다.")
  void schedule() throws InterruptedException {
    // given
    Runnable task = spy(SimpleMessageLoggerTask.create("Test!!!!..."));

    // when
    executeTaskOnSchedulerAfterOneSeconds(task);
    Thread.sleep(2_000);

    // then
    then(task).should(times(3)).run();
  }

  private void executeTaskOnSchedulerAfterOneSeconds(Runnable task) {
    Date startTime = new Date(System.currentTimeMillis() + 1_000);

    taskScheduler.schedule(task, startTime);
    taskScheduler.schedule(task, startTime);
    taskScheduler.schedule(task, startTime);
  }

  @Test
  @DisplayName("스케쥴러는 지정된 주기마다 예약된 작업을 실행한다.")
  void scheduleWithFixedDelay() throws InterruptedException {
    // given
    Runnable task = spy(SimpleMessageLoggerTask.create("Test!!!..."));

    // when
    taskScheduler.scheduleWithFixedDelay(task, 100);
    Thread.sleep(1_000);

    // then
    then(task).should(atLeastOnce()).run();
  }

  static class SimpleMessageLoggerTask implements Runnable {
    private static final String FORMAT_OF_MESSAGE = "[%s] Runnable task with message: %s";

    private final String message;

    private SimpleMessageLoggerTask(String message) {
      this.message = message;
    }

    static SimpleMessageLoggerTask create(String message) {
      return new SimpleMessageLoggerTask(message);
    }

    @Override
    public void run() {
      Thread thread = Thread.currentThread();
      String currentThreadName = thread.getName();
      log.info(String.format(FORMAT_OF_MESSAGE, currentThreadName, message));
    }
  }
}