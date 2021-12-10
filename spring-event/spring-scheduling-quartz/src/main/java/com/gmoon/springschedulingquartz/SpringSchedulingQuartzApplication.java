package com.gmoon.springschedulingquartz;

import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

@SpringBootApplication
@RequiredArgsConstructor
public class SpringSchedulingQuartzApplication implements ApplicationListener {
  private final Scheduler scheduler;

  public static void main(String[] args) {
    SpringApplication.run(SpringSchedulingQuartzApplication.class, args);
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    try {
      scheduler.start();
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }
}
