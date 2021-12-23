package com.gmoon.springquartzcluster.job;

import java.time.LocalDateTime;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.gmoon.springquartzcluster.service_check.ServiceStatusCheckService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerStatusCheckJob extends QuartzJobBean {
	private final ServiceStatusCheckService service;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobDetail = context.getJobDetail();
		JobKey key = jobDetail.getKey();
		String jobName = key.getName();

		log.info("start {}, {}", jobName, LocalDateTime.now());
		service.checkAll();
	}
}
