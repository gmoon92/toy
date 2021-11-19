package com.gmoon.springschedulingquartz.basic;

import com.gmoon.springschedulingquartz.basic.job.SimpleJobA;
import com.gmoon.springschedulingquartz.basic.job.SimpleJobB;
import com.gmoon.springschedulingquartz.basic.job.SimpleJobC;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SimpleSchedulerTest {
  final int SECONDS_OF_INTERVAL = 1;
  final int MIN_OF_PRIORITY = Integer.MIN_VALUE;

  Scheduler scheduler;

  @BeforeEach
  void setUp() throws SchedulerException {
    SchedulerFactory factory = new StdSchedulerFactory();
    scheduler = factory.getScheduler();
  }

  @Test
  @DisplayName("스케쥴러에 실행될 `Job`은 " +
          "`JobDataMap`을 통해 필요한 데이터를 접근할 수 있다.")
  void jobDataMapOfJobDetail() throws SchedulerException {
    // given
    JobDataMap dataMap = new JobDataMap();
    dataMap.put("username", "gmoon");
    dataMap.put("role", 0L);
    dataMap.put("isInfiniteRepeatMode", true);

    // when
    addJob(SimpleJobA.class, "group1", dataMap);
    scheduler.start();

    // then
    assertThat(scheduler.isStarted()).isTrue();
  }

  @Test
  @DisplayName("`Trigger`에 우선 순위(priority, default 5)를 지정할 수 있다." +
          "만약 스케쥴러가 실행될 수 없을 경우(Thread 자원이 없을 경우), " +
          "우선 순위 가장 높은 `Job`이 실행된다.")
  void priorityOfTrigger() throws SchedulerException {
    // given
    JobDataMap dataMap = new JobDataMap();

    // when
    addJob(SimpleJobB.class, "group1", dataMap, 15);
    addJob(SimpleJobC.class, "group2", dataMap, 20);
    scheduler.start();

    // then
    assertThat(scheduler.isStarted()).isTrue();
  }

  private void addJob(Class<? extends Job> jobClass, String nameOfGroup,
                      JobDataMap dataMap) {
    addJob(jobClass, nameOfGroup, dataMap, MIN_OF_PRIORITY);
  }

  private void addJob(Class<? extends Job> jobClass, String nameOfGroup,
                      JobDataMap dataMap, Integer priority) {
    JobKey jobKey = createJobKey(jobClass, nameOfGroup);
    JobDetail jobDetail = createJobDetail(jobClass, jobKey, dataMap);
    Trigger trigger = createTrigger(jobKey, priority);

    try {
      scheduler.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException e) {
      log.error("Can not be added Job or Trigger.", e);
    }
  }

  private JobKey createJobKey(Class<? extends Job> jobClass, String nameOfGroup) {
    String nameOfJob = jobClass.getName();
    return new JobKey(nameOfJob, nameOfGroup);
  }

  private JobDetail createJobDetail(Class<? extends Job> jobClass, JobKey jobKey, JobDataMap dataMap) {
    return JobBuilder.newJob(jobClass)
            .withIdentity(jobKey)
            .usingJobData(dataMap)
            .build();
  }

  private Trigger createTrigger(JobKey jobKey, Integer priority) {
    String nameOfTrigger = String.format("trigger_%s", jobKey.getName());
    return TriggerBuilder.newTrigger()
            .withIdentity(nameOfTrigger, jobKey.getGroup())
            .withPriority(priority)
            .withSchedule(getSchedulerBuilder())
            .startNow()
            .build();
  }

  private SimpleScheduleBuilder getSchedulerBuilder() {
    return SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(SECONDS_OF_INTERVAL)
            .repeatForever();
  }
}