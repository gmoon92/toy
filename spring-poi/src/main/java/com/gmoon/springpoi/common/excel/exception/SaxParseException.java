package com.gmoon.springpoi.common.excel.exception;

import java.nio.file.Path;

public class SaxParseException extends RuntimeException {
	public SaxParseException(Path path, Throwable cause) {
		super(String.format("An error occurred while parsing the Excel file: '%s'.", path), cause);
	}
}
