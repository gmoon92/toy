package com.gmoon.springschedulingquartz.service_check;

@FunctionalInterface
public interface ServiceStatusChecker {
  void check();
}
