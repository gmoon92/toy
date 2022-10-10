package com.gmoon.springeventlistener.orders.cart.domain.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ProductNo implements Serializable {

	@Column(name = "product_no", length = 50, nullable = false)
	private String value;

	public ProductNo(String value) {
		this.value = value;
	}
}
