package com.gmoon.springpoi.common.excel.exception;

import java.io.Serial;
import java.nio.file.Path;

import com.gmoon.springpoi.common.exception.InvalidFileException;

public class ExcelInvalidFileException extends InvalidFileException {
	@Serial
	private static final long serialVersionUID = 5120096218524168028L;

	public ExcelInvalidFileException(Path filePath) {
		super(String.format("invalid excel file: %s", filePath.toAbsolutePath()));
	}

	public ExcelInvalidFileException(String message, Throwable e) {
		super(message, e);
	}
}
