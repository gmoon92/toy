package com.gmoon.timesorteduniqueidentifier.idgenerator.exception;

import java.io.Serial;

public class InvalidSystemClock extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -3886118195005163739L;

	public InvalidSystemClock(String message) {
		super(message);
	}
}
