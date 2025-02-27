package com.gmoon.restassured.user;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
