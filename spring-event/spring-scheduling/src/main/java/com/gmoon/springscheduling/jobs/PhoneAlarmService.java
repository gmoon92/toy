package com.gmoon.springscheduling.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PhoneAlarmService {
  private static final int MILLISECONDS_OF_PLUS_TIME = 500;

  private long delay = 0;

  public void wakeUp() {
    final long now = System.currentTimeMillis() / 1000;
    log.info("schedule phone alarm tasks with dynamic delay - {}", now);
  }

  public long plusOneSecondsDelay() {
    this.delay += MILLISECONDS_OF_PLUS_TIME;
    log.info("delaying {} milliseconds...", this.delay);
    return this.delay;
  }
}
