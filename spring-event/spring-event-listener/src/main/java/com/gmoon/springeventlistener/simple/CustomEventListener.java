package com.gmoon.springeventlistener.simple;

import static java.lang.Thread.*;

import java.time.Duration;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomEventListener implements ApplicationListener<CustomEvent> {

	// by default Spring events are synchronous
	@Override
	public void onApplicationEvent(CustomEvent event) {
		log.info("start...");
		log.info("Received event({}): {}", event.getSource(), event.getMessage());
		slowProcess();
		log.info("end...");
	}

	private void slowProcess() {
		try {
			sleep(Duration.ofMillis(500).toMillis());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
