package com.gmoon.springdataredis.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotFoundDataException extends RuntimeException {
	private static final String MESSAGE_FORMAT = "key %s not be found on the redis server.";

	public NotFoundDataException(String key) {
		super(String.format(MESSAGE_FORMAT, key));
	}

	public NotFoundDataException(String key, Throwable cause) {
		super(String.format(MESSAGE_FORMAT, key), cause);
	}
}
