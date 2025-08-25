package com.gmoon.springpoi.common.excel.validator;

public interface ExcelValidator extends ExcelValidationErrorMessage {
	boolean isValid(String cellValue);
}
