package com.gmoon.springsecurityjwt.user;

import org.springframework.security.core.GrantedAuthority;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role implements GrantedAuthority {
	ADMIN,
	MANAGER,
	USER;

	public String getAuthority() {
		return String.format("ROLE_%s", name());
	}
}
