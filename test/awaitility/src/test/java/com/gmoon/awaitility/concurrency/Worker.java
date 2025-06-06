package com.gmoon.awaitility.concurrency;

import java.util.concurrent.CountDownLatch;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class Worker implements Runnable {

	private CountDownLatch latch;
	private Runnable executable;

	public Worker(CountDownLatch latch, Runnable executable) {
		this.latch = latch;
		this.executable = executable;
	}

	@Override
	public void run() {
		executable.run();
		countDown();
	}

	private void countDown() {
		latch.countDown();
		Thread thread = Thread.currentThread();
		log.info("[{}] tid: {}", thread.getName(), thread.getId());
	}
}
