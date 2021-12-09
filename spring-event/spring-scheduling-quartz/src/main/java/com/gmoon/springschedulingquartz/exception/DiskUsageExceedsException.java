package com.gmoon.springschedulingquartz.exception;

public class DiskUsageExceedsException extends RuntimeException {
  public DiskUsageExceedsException() {
    super("서버 저장 공간이 부족합니다.");
  }
}
