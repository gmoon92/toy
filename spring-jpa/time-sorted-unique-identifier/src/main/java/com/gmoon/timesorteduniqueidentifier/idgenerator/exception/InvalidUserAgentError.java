package com.gmoon.timesorteduniqueidentifier.idgenerator.exception;

import java.io.Serial;

// Custom exceptions for invalid user agent and system clock errors
public class InvalidUserAgentError extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -7797123620622822059L;

	public InvalidUserAgentError(String message) {
		super(message);
	}
}
