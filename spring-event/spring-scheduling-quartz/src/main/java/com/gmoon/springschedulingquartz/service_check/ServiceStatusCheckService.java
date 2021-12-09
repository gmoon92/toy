package com.gmoon.springschedulingquartz.service_check;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceStatusCheckService {
  private final List<ServiceStatusChecker> serviceStatusCheckers;

  public void checkAll() {
    for (ServiceStatusChecker executor : serviceStatusCheckers) {
      log.info("service check executor name: {}", executor.getClass().getSimpleName());
      executor.check();
    }
  }
}
