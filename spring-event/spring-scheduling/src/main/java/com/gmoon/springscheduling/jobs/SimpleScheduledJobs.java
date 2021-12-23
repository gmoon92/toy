package com.gmoon.springscheduling.jobs;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleScheduledJobs {
	private static final String FORMAT_OF_TASK_MESSAGE = "schedule tasks using {} jobs - {}";

	@Async
	@Scheduled(fixedDelay = 100)
	public void fixedDelayTask() {
		log.info(FORMAT_OF_TASK_MESSAGE, "fixedDelayTask", getCurrentSeconds());
	}

	@Async
	@Scheduled(fixedDelay = 100, initialDelay = 10)
	public void fixedDelayWithInitialDelayTask() {
		log.info(FORMAT_OF_TASK_MESSAGE, "fixedDelayWithInitialDelayTask", getCurrentSeconds());
	}

	@Async
	@Scheduled(fixedRate = 100)
	public void fixedRateTask() {
		log.info(FORMAT_OF_TASK_MESSAGE, "fixedRateTask", getCurrentSeconds());
	}

	@Async
	@Scheduled(cron = "* * * * * *", zone = "Asia/Seoul")
	public void taskUsingCronExpression() {
		log.info(FORMAT_OF_TASK_MESSAGE, "taskUsingCronExpression", getCurrentSeconds());
	}

	@Async
	@Scheduled(fixedDelayString = "${schedule.gmoon.fixedDelay.ms}")
	public void fixedDelayTaskUsingExpression() {
		log.info(FORMAT_OF_TASK_MESSAGE, "fixedDelayTaskUsingExpression", getCurrentSeconds());
	}

	@Async
	@Scheduled(fixedRateString = "${schedule.gmoon.fixedRate.ms}")
	public void fixedRateTaskUsingExpression() {
		log.info(FORMAT_OF_TASK_MESSAGE, "fixedRateTaskUsingExpression", getCurrentSeconds());
	}

	@Async
	@Scheduled(cron = "${schedule.gmoon.cron.expression}", zone = "Asia/Seoul")
	public void taskUsingExternalizedCronExpression() {
		log.info(FORMAT_OF_TASK_MESSAGE, "taskUsingExternalizedCronExpression", getCurrentSeconds());
	}

	private long getCurrentSeconds() {
		return System.currentTimeMillis() / 1_000;
	}
}
