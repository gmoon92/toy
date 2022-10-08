package com.gmoon.springeventlistener.events;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderListener {

	@Async
	@EventListener
	public void complete(CompletedOrderEvent event) {
		sendMessage(event);
	}

	private void sendMessage(CompletedOrderEvent event) {
		log.info("Handling send message... {}", event);
	}
}
