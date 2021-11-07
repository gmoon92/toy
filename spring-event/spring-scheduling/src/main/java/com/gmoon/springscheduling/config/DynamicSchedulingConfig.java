package com.gmoon.springscheduling.config;

import com.gmoon.springscheduling.jobs.PhoneAlarmJobs;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@ConditionalOnProperty(value = "schedule.type", havingValue = "dynamic")
@RequiredArgsConstructor
public class DynamicSchedulingConfig implements SchedulingConfigurer {

  private final PhoneAlarmJobs alarmJobs;

  @Bean
  public Executor taskExecutor() {
    return Executors.newSingleThreadScheduledExecutor();
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar registrar) {
    registrar.setScheduler(taskExecutor());
    registrar.addTriggerTask(alarmJobs::wakeUp, getDynamicDelayTimeTrigger());
  }

  private Trigger getDynamicDelayTimeTrigger() {
    return context -> {
      Optional<Date> lastCompletionTime = Optional.ofNullable(context.lastCompletionTime());
      Instant nextExecutionTime = lastCompletionTime.orElseGet(Date::new)
              .toInstant()
              .plusMillis(alarmJobs.plusOneSecondsDelay());
      return Date.from(nextExecutionTime);
    };
  }
}
