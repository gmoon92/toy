package com.gmoon.test.fixturemonkey.orders;

import java.math.BigDecimal;

import com.gmoon.test.fixturemonkey.products.Product;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
public class OrderLine {

	private String id;
	private Product product;
	private Integer quantity;
	private BigDecimal price;
}
