package com.gmoon.springsecurityjwt.jwt.exception;

public class InvalidAuthTokenException extends RuntimeException {
	public InvalidAuthTokenException(String message) {
		super(message);
	}

	public InvalidAuthTokenException(String token, Throwable cause) {
		super(String.format("Invalid signature or claims. token: %s", token), cause);
	}
}
