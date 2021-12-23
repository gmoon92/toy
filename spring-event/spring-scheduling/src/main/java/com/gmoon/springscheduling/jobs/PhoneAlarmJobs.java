package com.gmoon.springscheduling.jobs;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.gmoon.springscheduling.jobs.exception.PhoneAlarmSchedulerException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PhoneAlarmJobs {
	private static final int MILLISECONDS_OF_PLUS_TIME = 500;
	private static final int MAX_MILLISECONDS_OF_SLEEP_ON_PHONE = 3_000;

	private long delay = 0;

	@Async
	public void wakeUp() {
		if (delay > MAX_MILLISECONDS_OF_SLEEP_ON_PHONE) {
			throw new PhoneAlarmSchedulerException(MAX_MILLISECONDS_OF_SLEEP_ON_PHONE);
		}

		final long now = System.currentTimeMillis() / 1000;
		log.info("schedule phone alarm tasks with dynamic delay - {}", now);
	}

	public long getPlusSecondsDelay() {
		this.delay += MILLISECONDS_OF_PLUS_TIME;
		log.info("delaying {} milliseconds...", this.delay);
		return this.delay;
	}
}
