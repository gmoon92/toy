package com.gmoon.springpoi.common.exception;

public class InvalidFileException extends RuntimeException {
	public InvalidFileException(String message) {
		super(message);
	}

	public InvalidFileException(String message, Throwable cause) {
		super(message, cause);
	}
}
