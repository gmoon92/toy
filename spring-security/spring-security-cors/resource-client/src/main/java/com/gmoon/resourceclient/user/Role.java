package com.gmoon.resourceclient.user;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role implements GrantedAuthority {
	RESOURCE_SERVER("ROLE_RESOURCE_SERVER");

	private final String authority;
}
