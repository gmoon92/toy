package com.gmoon.springscheduling.jobs.exception;

public class PhoneAlarmSchedulerException extends RuntimeException {
  private static final String MESSAGE_FORMAT = "알람 최대 지연 시간 %d ms를 지났습니다.";

  public PhoneAlarmSchedulerException(long maxOfDelayMilliseconds) {
    super(String.format(MESSAGE_FORMAT, maxOfDelayMilliseconds));
  }
}
