package com.gmoon.springeventlistener.orders.events;

import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderListener {

	@Order(Ordered.LOWEST_PRECEDENCE)
	@Async
	@EventListener(condition = "#event.orderNo != null && !#event.orderNo.isEmpty()")
	public void syncOrderLines(CompletedOrderEvent event) {
		log.info("Handling synchronized order lines ... {}", event);
	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Async
	@EventListener(condition = "#event.orderNo != null && !#event.orderNo.isEmpty()")
	public void sendMessage(CompletedOrderEvent event) {
		log.info("Handling send message... {}", event);
	}
}
