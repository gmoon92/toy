package com.gmoon.test.fixturemonkey.products;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product {
	private String id;
	private String name;
	private Integer quantity;
	private BigDecimal price;
	private Category category;
	private String description;
}
