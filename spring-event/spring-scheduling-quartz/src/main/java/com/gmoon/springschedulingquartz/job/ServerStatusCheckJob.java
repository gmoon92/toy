package com.gmoon.springschedulingquartz.job;

import com.gmoon.springschedulingquartz.service_check.ServiceStatusCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
