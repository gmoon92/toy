package com.gmoon.springeventlistener.orders;

import java.util.UUID;

import com.gmoon.springeventlistener.orders.order.domain.OrderLineItem;
import com.gmoon.springeventlistener.orders.order.domain.vo.Product;
import com.gmoon.springeventlistener.orders.order.domain.vo.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Fixtures {

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
