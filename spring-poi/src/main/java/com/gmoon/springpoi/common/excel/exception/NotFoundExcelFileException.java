package com.gmoon.springpoi.common.excel.exception;

import java.io.File;

public class NotFoundExcelFileException extends RuntimeException {
	public NotFoundExcelFileException(File file) {
		super(String.format("Excel file not found: %s", file != null ? file.getAbsolutePath() : "null")
		);
	}
}
