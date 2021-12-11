package com.gmoon.springquartzcluster.service_check;

@FunctionalInterface
public interface ServiceStatusChecker {
  void check();
}
