package com.gmoon.springpoi.common.excel.exception;

import java.io.Serial;

public class ExcelEmptyDataRowException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = -3681665722064623654L;

	public ExcelEmptyDataRowException() {
		super("No data rows found in the Excel file during processing.");
	}
}
