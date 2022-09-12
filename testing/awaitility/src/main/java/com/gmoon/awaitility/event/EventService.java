package com.gmoon.awaitility.event;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

	private final EventRepository repository;

	@Async
	public Event save(Event event) {
		sleep(Duration.ofMillis(500));
		return repository.save(event);
	}

	public int count() {
		return repository.count();
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	@Async
	public void handle() {
		sleep(Duration.ofSeconds(1));
		Thread thread = Thread.currentThread();
		log.info("[{}] tid: {}", thread.getName(), thread.getId());
	}

	private void sleep(Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
