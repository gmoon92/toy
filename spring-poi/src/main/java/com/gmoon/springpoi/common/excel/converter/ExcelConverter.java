package com.gmoon.springpoi.common.excel.converter;

@FunctionalInterface
public interface ExcelConverter<T> {
	T convert(String value);
}
