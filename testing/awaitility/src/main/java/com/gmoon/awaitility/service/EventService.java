package com.gmoon.awaitility.service;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventService {

	@Async
	public void handle() {
		doWork();

		Thread thread = Thread.currentThread();
		log.info("[{}] tid: {}", thread.getName(), thread.getId());
	}

	private void doWork() {
		try {
			Thread.sleep(Duration.ofMillis(100).toMillis());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
