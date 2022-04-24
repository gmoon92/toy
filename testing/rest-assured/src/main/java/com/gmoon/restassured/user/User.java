package com.gmoon.restassured.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
	@EqualsAndHashCode.Include
	private String name;

	private boolean enabled;

	private User(String name) {
		this(name, true);
	}

	public static User from(String name) {
		return new User(name);
	}

	public User disabled() {
		enabled = false;
		return this;
	}
}
