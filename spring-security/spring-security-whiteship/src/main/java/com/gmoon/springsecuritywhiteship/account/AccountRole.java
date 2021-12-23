package com.gmoon.springsecuritywhiteship.account;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountRole implements GrantedAuthority {
	ADMIN("ADMIN", "ROLE_ADMIN"),
	USER("USER", "ROLE_USER");

	private final String role;
	private final String authority;
}
