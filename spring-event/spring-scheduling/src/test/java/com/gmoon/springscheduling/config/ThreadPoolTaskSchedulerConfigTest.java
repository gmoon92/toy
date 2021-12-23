package com.gmoon.springscheduling.config;

import static org.mockito.BDDMockito.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@EnabledIf(expression = "#{'${schedule.type}' == 'default'}", loadContext = true)
class ThreadPoolTaskSchedulerConfigTest {

	@Autowired
	ThreadPoolTaskScheduler taskScheduler;

	Runnable task;

	@BeforeEach
	void setUp() {
		task = spy(SimpleMessageLoggerTask.create("Test!!!!..."));
	}

	@Test
	@DisplayName("스케쥴러는 1초뒤에 예약된 작업을 실행한다.")
	void schedule() throws InterruptedException {
		// given when
		executeTaskOnSchedulerAfterOneSeconds(task);
		Thread.sleep(2_000);

		// then
		then(task).should(times(3)).run();
	}

	private void executeTaskOnSchedulerAfterOneSeconds(Runnable task) {
		Date startTime = new Date(System.currentTimeMillis() + 1_000);

		taskScheduler.schedule(task, startTime);
		taskScheduler.schedule(task, startTime);
		taskScheduler.schedule(task, startTime);
	}

	@Test
	@DisplayName("스케쥴러는 지정된 주기마다 예약된 작업을 실행한다.")
	void scheduleWithFixedDelay() throws InterruptedException {
		// given when
		taskScheduler.scheduleWithFixedDelay(task, 100);
		Thread.sleep(1_000);

		// then
		then(task).should(atLeastOnce()).run();
	}

	@Test
	@DisplayName("스케쥴러는 지정된 일자 이후에, 지정된 주기마다 예약된 작업을 실행한다.")
	void scheduleWithFixedDelayWithInitialDelayTask() throws InterruptedException {
		// given when
		taskScheduler.scheduleWithFixedDelay(task, new Date(), 100);
		Thread.sleep(1_000);

		// then
		then(task).should(atLeastOnce()).run();
	}

	@Test
	@DisplayName("스케쥴러는 다음 작업 결과와 상관없이, 수행할 작업을 실행한다.")
	void scheduleAtFixedRate() throws InterruptedException {
		// given when
		taskScheduler.scheduleAtFixedRate(task, 100);
		Thread.sleep(1_000);

		// then
		then(task).should(atLeastOnce()).run();
	}

	@Test
	@DisplayName("스케쥴러는 지정된 일자 이후에, 다음 작업 결과와 상관없이, 수행할 작업을 실행한다.")
	void scheduleAtFixedRateWithInitialDelayTask() throws InterruptedException {
		// given when
		taskScheduler.scheduleAtFixedRate(task, new Date(), 100);
		Thread.sleep(1_000);

		// then
		then(task).should(atLeastOnce()).run();
	}

	@Test
	@DisplayName("cron 표현식으로 스케쥴러 작업을 수행한다.")
	void cronTrigger() throws InterruptedException {
		// given
		CronTrigger cron = new CronTrigger("* * * * * *");

		// when
		taskScheduler.schedule(task, cron);
		Thread.sleep(1_100);

		// then
		then(task).should(atLeastOnce()).run();
	}

	@Test
	@DisplayName("PeriodicTrigger를 사용하여 스케쥴러 작업을 수행한다.")
	void periodicTrigger() throws InterruptedException {
		// given
		PeriodicTrigger trigger = new PeriodicTrigger(100, TimeUnit.MILLISECONDS);
		trigger.setFixedRate(false);
		trigger.setInitialDelay(500);

		// when
		taskScheduler.schedule(task, trigger);
		Thread.sleep(1_000);

		// then
		then(task).should(atLeastOnce()).run();
	}

	static class SimpleMessageLoggerTask implements Runnable {
		private static final String FORMAT_OF_MESSAGE = "[%s] Runnable task with message: %s";

		private final String message;

		private SimpleMessageLoggerTask(String message) {
			this.message = message;
		}

		static SimpleMessageLoggerTask create(String message) {
			return new SimpleMessageLoggerTask(message);
		}

		@Override
		public void run() {
			Thread thread = Thread.currentThread();
			String currentThreadName = thread.getName();
			log.info(String.format(FORMAT_OF_MESSAGE, currentThreadName, message));
		}
	}
}
