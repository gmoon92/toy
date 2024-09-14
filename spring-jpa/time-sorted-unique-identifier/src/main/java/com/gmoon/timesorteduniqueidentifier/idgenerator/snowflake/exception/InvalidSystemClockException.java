package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake.exception;

import java.io.Serial;

public class InvalidSystemClockException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -3886118195005163739L;

	public InvalidSystemClockException(String message) {
		super(message);
	}
}
