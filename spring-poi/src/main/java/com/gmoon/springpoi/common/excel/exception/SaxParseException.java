package com.gmoon.springpoi.common.excel.exception;

import java.io.Serial;
import java.nio.file.Path;

public class SaxParseException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 8320295476703215193L;

	public SaxParseException(Path path, Throwable cause) {
		super(String.format("An error occurred while parsing the Excel file: '%s'.", path), cause);
	}
}
