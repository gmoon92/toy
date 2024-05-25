package com.gmoon.springeventlistener.orders.order.domain.vo;

import java.io.Serializable;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Product implements Serializable {

	@Column(name = "product_no", length = 50, nullable = false)
	private String id;

	@Column(name = "product_name", length = 50, nullable = false)
	private String name;

	@Column(name = "product_price", nullable = false)
	@ColumnDefault("0")
	private long price;

	public Product(String id, String name, long price) {
		this.id = id;
		this.name = name;
		this.price = price;
	}
}
