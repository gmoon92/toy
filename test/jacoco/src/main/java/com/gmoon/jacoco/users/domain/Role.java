package com.gmoon.jacoco.users.domain;

import org.springframework.security.core.GrantedAuthority;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role implements GrantedAuthority {

	ADMIN(Constants.ADMIN),
	USER(Constants.USER);

	private final String value;

	@Override
	public String getAuthority() {
		return "ROLE_" + value;
	}

	public static class Constants {
		public static final String ADMIN = "ADMIN";
		public static final String USER = "USER";
	}
}
