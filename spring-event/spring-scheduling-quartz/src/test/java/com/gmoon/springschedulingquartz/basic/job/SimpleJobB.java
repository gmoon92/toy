package com.gmoon.springschedulingquartz.basic.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
public class SimpleJobB implements Job {

  @Override
  public void execute(JobExecutionContext context) {
    log.info("{} Job execute...", getClass().getName());
  }
}
