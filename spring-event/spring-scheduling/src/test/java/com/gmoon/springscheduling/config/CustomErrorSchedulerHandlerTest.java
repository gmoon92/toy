package com.gmoon.springscheduling.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class CustomErrorSchedulerHandlerTest {

  @Autowired
  ApplicationContext applicationContext;

  @Autowired
  ScheduledTaskHolder scheduledTaskHolder;

  @Test
  @DisplayName("5.0.2 부터 도입된 ScheduledTaskHolder 를 통해 " +
          "등록된 스케쥴러 테스크를 확인한다.")
  void getScheduledTasks() {
    // given
    Set<ScheduledTask> tasks = scheduledTaskHolder.getScheduledTasks();

    // when
    ScheduledAnnotationBeanPostProcessor postProcessor = applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);

    // then
    assertThat(tasks)
            .isNotEmpty()
            .containsAll(postProcessor.getScheduledTasks());
  }

  @Test
  @Disabled
  @DisplayName("3초 이상 알람이 실행시 강제 이셉션이 발생된다. 스케쥴러 이셉션을 로깅한다.")
  void handleError() throws InterruptedException {
    Set<ScheduledTask> tasks = scheduledTaskHolder.getScheduledTasks();

    while (true) {
      printStatusOfScheduledTasks(tasks);
      Thread.sleep(1000);
    }
  }

  private void printStatusOfScheduledTasks(Set<ScheduledTask> tasks) {
    for (ScheduledTask scheduledTask : tasks) {
      Task task = getTask(scheduledTask);
      log.info("task type is {}", task.getClass());
      log.info("task: {}", task);
    }
  }

  private <T extends Task> T getTask(ScheduledTask scheduledTask) {
    final Class<T>[] ALLOWED_TASK_TYPES = new Class[]{ TriggerTask.class,
            CronTask.class,
            FixedDelayTask.class, FixedRateTask.class };

    Task task = scheduledTask.getTask();
    return Arrays.stream(ALLOWED_TASK_TYPES)
            .filter(clazz -> clazz == task.getClass())
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("Not allowed task type is %s", task.getClass())))
            .cast(task);
  }
}