package com.gmoon.resourceserver.jwt.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtVerifyException extends AuthenticationException {
	public JwtVerifyException(Throwable cause) {
		super("JWT verification failed.", cause);
	}
}
