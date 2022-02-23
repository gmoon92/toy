package com.gmoon.resourceclient.security;

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import com.gmoon.resourceclient.user.Role;

public class OAuthenticationToken extends AbstractAuthenticationToken {
	private final String token;

	public OAuthenticationToken(String token) {
		super(Collections.singleton(Role.RESOURCE_SERVER));
		this.token = token;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return token;
	}
}
