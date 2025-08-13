package com.gmoon.springpoi.common.excel.validator;

@FunctionalInterface
public interface ExcelValidator {
	boolean isValid(String cellValue);
}
