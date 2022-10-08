package com.gmoon.springeventlistener.events;

import java.io.Serializable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
}
