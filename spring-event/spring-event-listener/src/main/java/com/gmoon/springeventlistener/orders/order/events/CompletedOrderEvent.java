package com.gmoon.springeventlistener.orders.order.events;

import java.io.Serializable;
import java.util.List;

import com.gmoon.springeventlistener.orders.order.domain.Order;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class CompletedOrderEvent implements Serializable {

	private final String orderNo;
	private final List<String> productNos;
	private final long orderPrice;
	private final String productName;
	private final String userName;
	private final String userEmail;

	@Builder
	private CompletedOrderEvent(String orderNo, long orderPrice, String productName, String userName, String userEmail,
		 List<String> productNos) {
		this.orderNo = orderNo;
		this.productNos = productNos;
		this.orderPrice = orderPrice;
		this.productName = productName;
		this.userName = userName;
		this.userEmail = userEmail;
	}

	public static CompletedOrderEvent create(Order order) {
		return CompletedOrderEvent.builder()
			 .orderNo(order.getId())
			 .productNos(order.getProductNos())
			 .orderPrice(order.totalPrice())
			 .productName(order.getProductNames())
			 .userName(order.getUser().getUserName())
			 .userEmail(order.getUser().getUserEmail())
			 .build();
	}
}
