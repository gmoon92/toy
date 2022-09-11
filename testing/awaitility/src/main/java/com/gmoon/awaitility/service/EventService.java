package com.gmoon.awaitility.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventService {

	@Async
	public void handle(String message) {
		log.info("send message: {}", message);
	}
}
