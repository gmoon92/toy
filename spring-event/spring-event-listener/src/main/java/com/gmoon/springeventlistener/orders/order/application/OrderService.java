package com.gmoon.springeventlistener.orders.order.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springeventlistener.orders.order.domain.JpaOrderRepository;
import com.gmoon.springeventlistener.orders.order.domain.Order;
import com.gmoon.springeventlistener.orders.order.events.CompletedOrderEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final ApplicationEventPublisher publisher;
	private final JpaOrderRepository orderRepository;

	@Transactional
	public Order complete(Order order) {
		order.complete();

		orderRepository.save(order);

		publisher.publishEvent(CompletedOrderEvent.create(order));
		return order;
	}
}
