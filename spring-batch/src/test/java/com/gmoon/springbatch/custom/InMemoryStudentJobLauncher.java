package com.gmoon.springbatch.custom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@TestComponent
@RequiredArgsConstructor
public class InMemoryStudentJobLauncher {

	@Qualifier("inMemoryStudentJob")
	private final Job job;

	private final JobLauncher jobLauncher;

	@Scheduled(cron = "${in.memory.reader.job.cron}")
	void launchXmlFileToDatabaseJob() throws JobParametersInvalidException,
		 JobExecutionAlreadyRunningException,
		 JobRestartException,
		 JobInstanceAlreadyCompleteException
	{
		log.info("Starting inMemoryStudentJob job");
		jobLauncher.run(job, newExecution());
		log.info("Stopping inMemoryStudentJob job");
	}

	private JobParameters newExecution() {
		Map<String, JobParameter<?>> parameters = new HashMap<>();
		parameters.put("currentTime", new JobParameter<>(LocalDateTime.now(), LocalDateTime.class));
		return new JobParameters(parameters);
	}
}
