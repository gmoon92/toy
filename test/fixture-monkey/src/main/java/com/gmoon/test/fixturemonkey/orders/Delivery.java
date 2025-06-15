package com.gmoon.test.fixturemonkey.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Delivery {
	private String id;
	private Recipient recipient;
	private List<OrderLine> orderLines = new ArrayList<>();
	private BigDecimal price;
}
