package com.gmoon.springpoi.common.excel.exception;

public class ExcelExceedRowLimitException extends RuntimeException {

	public ExcelExceedRowLimitException(long maxDataRows) {
		super("limit exceed data rows: " + maxDataRows);
	}
}
