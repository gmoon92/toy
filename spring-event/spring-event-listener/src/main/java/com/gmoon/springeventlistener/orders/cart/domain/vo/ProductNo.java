package com.gmoon.springeventlistener.orders.cart.domain.vo;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ProductNo implements Serializable {

	private String value;

	public ProductNo(String value) {
		this.value = value;
	}
}
