package com.gmoon.springscheduling.config;

import com.gmoon.springscheduling.jobs.exception.PhoneAlarmSchedulerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

@Slf4j
public class CustomErrorSchedulerHandler implements ErrorHandler {

  @Autowired
  ThreadPoolTaskScheduler threadPoolTaskScheduler;

  @Override
  public void handleError(Throwable t) {
    if (t instanceof PhoneAlarmSchedulerException) {
      log.error("Exception in scheduler task.", t);
    }
  }
}
