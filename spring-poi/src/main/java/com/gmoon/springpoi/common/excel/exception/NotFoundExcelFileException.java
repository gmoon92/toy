package com.gmoon.springpoi.common.excel.exception;

import java.io.File;
import java.io.Serial;

public class NotFoundExcelFileException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = -4005206634009326603L;

	public NotFoundExcelFileException(File file) {
		super(String.format("Excel file not found: %s", file != null ? file.getAbsolutePath() : "null")
		);
	}
}
