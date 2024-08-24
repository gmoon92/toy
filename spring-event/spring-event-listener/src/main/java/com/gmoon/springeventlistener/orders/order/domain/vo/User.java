package com.gmoon.springeventlistener.orders.order.domain.vo;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User implements Serializable {

	private String id;

	private String name;

	private String email;

	public User(String id, String name, String userEmail) {
		this.id = id;
		this.name = name;
		this.email = userEmail;
	}
}
