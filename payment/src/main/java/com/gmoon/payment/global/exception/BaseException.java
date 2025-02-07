package com.gmoon.payment.global.exception;

@SuppressWarnings("serial")
public abstract class BaseException extends RuntimeException {

	protected BaseException(String message) {
		super(message);
	}

	protected BaseException(Throwable cause) {
		super(cause);
	}

	protected BaseException(Throwable cause, String message) {
		super(message, cause);
	}

	public abstract String getErrorCode();

	public abstract String getDetailCode();
}
