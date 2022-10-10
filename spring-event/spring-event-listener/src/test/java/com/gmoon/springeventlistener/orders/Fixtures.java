package com.gmoon.springeventlistener.orders;

import java.util.UUID;

import com.gmoon.springeventlistener.orders.order.domain.OrderLineItem;
import com.gmoon.springeventlistener.orders.order.domain.vo.Product;
import com.gmoon.springeventlistener.orders.order.domain.vo.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Fixtures {

	public static final String ORDER_NO = "order0";
	public static final String ORDER_LINE_ITEM0 = "item0";
	public static final String ORDER_LINE_ITEM1 = "item1";
	public static final String USER_ID = "user0";
	public static final String PRODUCT_NO0 = "product0";
	public static final String PRODUCT_NO1 = "product1";

	public static User user() {
		return new User("0001", "gmoon", "gmoon0929@gmail.com");
	}

	public static OrderLineItem orderLineItem(int quantity, Product product) {
		return OrderLineItem.builder()
			.product(product)
			.quantity(quantity)
			.build();
	}

	public static Product product(String name, long price) {
		return new Product(
			UUID.randomUUID().toString(),
			name,
			price
		);
	}
}
