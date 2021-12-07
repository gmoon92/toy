package com.gmoon.springschedulingquartz.quartz.core;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.quartz.spi.InstanceIdGenerator;

import java.net.InetAddress;

@Slf4j
public class CustomInstanceIdGenerator implements InstanceIdGenerator {
  @Override
  public String generateInstanceId() throws SchedulerException {
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      String id = String.format("%s_%s", hostName, System.currentTimeMillis());
      log.info("scheduler generated id: {}", id);
      return id;
    } catch (Exception e) {
      throw new SchedulerException("Couldn't get host name!", e);
    }
  }
}
