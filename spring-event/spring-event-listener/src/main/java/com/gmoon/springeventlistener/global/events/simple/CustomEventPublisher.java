package com.gmoon.springeventlistener.global.events.simple;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomEventPublisher {

	private final ApplicationEventPublisher publisher;

	public void raise(String message) {
		CustomEvent event = new CustomEvent(this, message);
		publisher.publishEvent(event);
	}
}
