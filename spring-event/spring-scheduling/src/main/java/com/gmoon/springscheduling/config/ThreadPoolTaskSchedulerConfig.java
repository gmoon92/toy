package com.gmoon.springscheduling.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class ThreadPoolTaskSchedulerConfig {
  private static final String NAME_OF_THREAD_SCHEDULER_TASK = "ThreadPoolTaskScheduler";
  private static final int SIZE_OF_POOL = 5;

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setThreadNamePrefix(NAME_OF_THREAD_SCHEDULER_TASK);
    taskScheduler.setPoolSize(SIZE_OF_POOL);
    return taskScheduler;
  }
}
