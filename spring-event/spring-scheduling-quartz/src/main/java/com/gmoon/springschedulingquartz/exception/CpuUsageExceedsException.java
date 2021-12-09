package com.gmoon.springschedulingquartz.exception;

public class CpuUsageExceedsException extends RuntimeException {
  public CpuUsageExceedsException() {
    super("CPU 사용량 비정상적으로 높습니다.");
  }
}
