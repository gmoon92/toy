package com.gmoon.commons.commonsapachepoi.excel.converter;

@FunctionalInterface
public interface ExcelConverter<T> {
	T convert(String value);
}
