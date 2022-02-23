package com.gmoon.resourceclient.security;

import org.springframework.security.core.AuthenticationException;

public class OAuthLoginException extends AuthenticationException {
	public OAuthLoginException(Throwable cause) {
		super("OAuth login failure.", cause);
	}
}
