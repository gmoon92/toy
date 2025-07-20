package com.gmoon.springpoi.excel.validator;

@FunctionalInterface
public interface ExcelValidator {
	boolean isValid(String cellValue);
}
