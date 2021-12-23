package com.gmoon.springquartzcluster.quartz.core;

import java.net.InetAddress;

import org.quartz.SchedulerException;
import org.quartz.spi.InstanceIdGenerator;

import lombok.extern.slf4j.Slf4j;

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
