package com.gmoon.springeventlistener.orders.order.events;

import java.io.Serializable;

import com.gmoon.springeventlistener.orders.order.domain.Order;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class CompletedOrderEvent implements Serializable {

	private final String orderNo;
	private final long orderPrice;
	private final String productName;
	private final String userName;
	private final String userEmail;

	@Builder
	private CompletedOrderEvent(String orderNo, long orderPrice, String productName, String userName, String userEmail) {
		this.orderNo = orderNo;
		this.orderPrice = orderPrice;
		this.productName = productName;
		this.userName = userName;
		this.userEmail = userEmail;
	}

	public static CompletedOrderEvent create(Order order) {
		return CompletedOrderEvent.builder()
			.orderNo(order.getId())
			.orderPrice(order.totalPrice())
			.productName(order.getProductNames())
			.userName(order.getUser().getUserName())
			.userEmail(order.getUser().getUserEmail())
			.build();
	}
}
