package com.gmoon.springquartzcluster.config;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SimpleTrigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import com.gmoon.springquartzcluster.job.ServerStatusCheckJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class QuartzScheduleConfig {
	@Bean
	public JobDetailFactoryBean jobDetail() {
		JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
		jobDetail.setJobClass(ServerStatusCheckJob.class);
		jobDetail.setName(ServerStatusCheckJob.class.getSimpleName());
		jobDetail.setDescription("Service health check job.");
		jobDetail.setDurability(true);
		return jobDetail;
	}

	@Bean
	public SimpleTriggerFactoryBean trigger(JobDetail job) {
		JobKey key = job.getKey();
		String jobName = key.getName();

		SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setName("trigger_" + jobName);
		trigger.setJobDetail(job);
		trigger.setRepeatInterval(3_000);
		trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
		return trigger;
	}
}
