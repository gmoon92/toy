package com.gmoon.springsecurityjwt.jwt.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;

public class InvalidAuthTokenException extends JWTVerificationException {
	public InvalidAuthTokenException(String message) {
		super(message);
	}

	public InvalidAuthTokenException(String token, Throwable cause) {
		super(String.format("Invalid signature or claims. token: %s", token), cause);
	}
}
