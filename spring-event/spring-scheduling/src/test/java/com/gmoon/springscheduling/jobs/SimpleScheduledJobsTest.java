package com.gmoon.springscheduling.jobs;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class SimpleScheduledJobsTest {

	@SpyBean
	SimpleScheduledJobs simpleScheduledJobs;

	@Test
	void fixedDelayTask() throws InterruptedException {
		// given when
		Thread.sleep(200);

		// then
		then(simpleScheduledJobs).should(atLeastOnce()).fixedDelayTask();
	}

	@Test
	void fixedDelayWithInitialDelayTask() throws InterruptedException {
		// given when
		Thread.sleep(200);

		// then
		then(simpleScheduledJobs).should(atLeastOnce()).fixedDelayWithInitialDelayTask();
	}

	@Test
	void fixedRateTask() throws InterruptedException {
		// given when
		Thread.sleep(200);

		// then
		then(simpleScheduledJobs).should(atLeastOnce()).fixedRateTask();
	}

	@Test
	void taskUsingCronExpression() throws InterruptedException {
		// given when
		Thread.sleep(1_000);

		// then
		then(simpleScheduledJobs).should(atLeastOnce()).taskUsingCronExpression();
	}

	@Test
	void fixedDelayTaskUsingExpression() throws InterruptedException {
		// given when
		Thread.sleep(200);

		// then
		then(simpleScheduledJobs).should(atLeastOnce()).fixedDelayTaskUsingExpression();
	}

	@Test
	void fixedRateTaskUsingExpression() throws InterruptedException {
		// given when
		Thread.sleep(200);

		// then
		then(simpleScheduledJobs).should(atLeastOnce()).fixedRateTaskUsingExpression();
	}

	@Test
	void taskUsingExternalizedCronExpression() throws InterruptedException {
		// given when
		Thread.sleep(1_000);

		// then
		then(simpleScheduledJobs).should(atLeastOnce()).taskUsingExternalizedCronExpression();
	}
}
