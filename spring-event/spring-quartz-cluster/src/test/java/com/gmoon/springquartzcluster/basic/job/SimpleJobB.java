package com.gmoon.springquartzcluster.basic.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleJobB implements Job {

	@Override
	public void execute(JobExecutionContext context) {
		log.info("{} Job execute...", getClass().getName());
	}
}
