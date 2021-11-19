package com.gmoon.springschedulingquartz.basic.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

@Slf4j
public class SimpleJobA implements Job {

  @Override
  public void execute(JobExecutionContext context) {
    log.info("{} Job execute...", getClass().getName());

    JobDetail detail = context.getJobDetail();
    JobKey key = detail.getKey();
    String jobName = key.getName();
    String groupName = key.getGroup();
    log.info("jab: {}, group: {}", jobName, groupName);

    JobDataMap datas = context.getMergedJobDataMap();
    log.info("detail param. role: {}", datas.getLong("role"));
    log.info("detail param. username: {}", datas.getString("username"));
    log.info("trigger param. infinite repeat: {}", datas.getBoolean("isInfiniteRepeatMode"));
  }
}
