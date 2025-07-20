package com.gmoon.springpoi.excel.converter;

@FunctionalInterface
public interface ExcelConverter<T> {
	T convert(String value);
}
