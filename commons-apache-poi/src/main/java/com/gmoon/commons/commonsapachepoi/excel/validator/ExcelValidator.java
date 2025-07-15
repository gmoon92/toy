package com.gmoon.commons.commonsapachepoi.excel.validator;

@FunctionalInterface
public interface ExcelValidator {
	boolean isValid(String cellValue);
}
