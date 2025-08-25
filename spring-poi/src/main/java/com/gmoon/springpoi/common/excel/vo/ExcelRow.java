package com.gmoon.springpoi.common.excel.vo;

import java.lang.reflect.Field;

import com.gmoon.springpoi.common.excel.converter.ExcelConverter;
import com.gmoon.springpoi.common.utils.ReflectionUtil;

import lombok.Getter;

@Getter
public class ExcelRow<T> {
	private final long rowIdx;
	private final T excelVO;

	ExcelRow(long rowIdx, Class<T> clazz) {
		this.rowIdx = rowIdx;
		this.excelVO = ReflectionUtil.newInstance(clazz);
	}

	public void setFieldValue(ExcelField excelField, String cellValue) {
		try {
			ExcelConverter<?> converter = excelField.getConverter();
			Field field = excelField.getField();
			field.set(excelVO, converter.convert(cellValue));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
