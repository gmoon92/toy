package com.gmoon.springeventlistener.orders.cart.domain.vo;

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
public class UserId implements Serializable {

	private String value;

	public UserId(String value) {
		this.value = value;
	}
}
