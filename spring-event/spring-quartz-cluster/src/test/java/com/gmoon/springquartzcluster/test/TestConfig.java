package com.gmoon.springquartzcluster.test;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.gmoon.springquartzcluster.config.JpaConfig;

@Import(JpaConfig.class)
@TestConfiguration
public class TestConfig {
	@Bean
	public Scheduler scheduler() throws SchedulerException {
		SchedulerFactory factory = new StdSchedulerFactory();
		return factory.getScheduler();
	}
}
