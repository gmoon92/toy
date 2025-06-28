package com.gmoon.test.fixturemonkey.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Order {
	private String id;
	private OrderStatus status;

	private OrderBuyer buyer;
	private List<Delivery> deliveries = new ArrayList<>();

	private BigDecimal price;
	private Instant createdAt;
}
