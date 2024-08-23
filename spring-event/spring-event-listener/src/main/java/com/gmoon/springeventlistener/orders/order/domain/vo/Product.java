package com.gmoon.springeventlistener.orders.order.domain.vo;

import java.io.Serializable;

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

	private String no;
	private String name;
	private long price;

	public Product(String no, String name, long price) {
		this.no = no;
		this.name = name;
		this.price = price;
	}
}
