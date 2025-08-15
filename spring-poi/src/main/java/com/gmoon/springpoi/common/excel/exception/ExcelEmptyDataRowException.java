package com.gmoon.springpoi.common.excel.exception;

public class ExcelEmptyDataRowException extends RuntimeException {

	public ExcelEmptyDataRowException() {
		super("No data rows found in the Excel file during processing.");
	}
}
