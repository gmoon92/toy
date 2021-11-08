package com.gmoon.springscheduling.config;

import com.gmoon.springscheduling.jobs.PhoneAlarmJobs;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Configuration
@EnableScheduling
@ConditionalOnProperty(value = "schedule.type", havingValue = "dynamic")
@RequiredArgsConstructor
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
