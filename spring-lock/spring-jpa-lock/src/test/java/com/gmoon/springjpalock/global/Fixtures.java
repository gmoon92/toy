package com.gmoon.springjpalock.global;

import java.util.List;
import java.util.UUID;

import com.gmoon.springjpalock.orders.domain.Order;
import com.gmoon.springjpalock.orders.domain.OrderLineItem;
import com.gmoon.springjpalock.orders.domain.vo.OrderStatus;

public final class Fixtures {

	public static final String ORDER_NO = "order-no-001";

	public static Order newOrder(List<OrderLineItem> orderLineItems) {
		return Order.builder()
			.address(UUID.randomUUID().toString())
			.status(OrderStatus.WAITING)
			.orderLineItems(orderLineItems)
			.build();
	}
}
