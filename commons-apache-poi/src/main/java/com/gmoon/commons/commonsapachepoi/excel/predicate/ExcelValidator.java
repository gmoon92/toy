package com.gmoon.commons.commonsapachepoi.excel.predicate;

@FunctionalInterface
public interface ExcelValidator {
	boolean isValid(String cellValue);
}
