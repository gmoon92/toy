package com.gmoon.springsecurityjwt.exception;

public class JacksonConverterException extends RuntimeException {
	public JacksonConverterException(Throwable cause) {
		super("JSON conversion error.", cause);
	}
}
