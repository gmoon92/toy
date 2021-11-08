package com.gmoon.springscheduling.config;

import com.gmoon.springscheduling.jobs.SimpleScheduledJobs;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
@EnableScheduling
public class ThreadPoolTaskSchedulerConfig implements AsyncConfigurer {
  private static final String NAME_OF_THREAD_SCHEDULER_TASK = "scheduler-thread-gmoon-pool";
  private static final int SIZE_OF_POOL = 5;

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler(CustomErrorSchedulerHandler customErrorSchedulerHandler) {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setThreadNamePrefix(NAME_OF_THREAD_SCHEDULER_TASK);
    taskScheduler.setPoolSize(SIZE_OF_POOL);
    taskScheduler.setErrorHandler(customErrorSchedulerHandler);
    taskScheduler.initialize();
    return taskScheduler;
  }

  @Bean
  public CustomErrorSchedulerHandler customErrorSchedulerHandler() {
    return new CustomErrorSchedulerHandler();
  }

  @Bean
  @ConditionalOnProperty(value = "schedule.type", havingValue = "default")
  public SimpleScheduledJobs simpleScheduledJobs() {
    return new SimpleScheduledJobs();
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new CustomAsyncUncaughtExceptionHandler();
  }
}
