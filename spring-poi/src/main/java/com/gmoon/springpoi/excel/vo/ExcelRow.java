package com.gmoon.springpoi.excel.vo;

import java.lang.reflect.Field;

import com.gmoon.springpoi.common.utils.ReflectionUtil;
import com.gmoon.springpoi.excel.converter.ExcelConverter;

import lombok.Getter;

@Getter
public class ExcelRow<T> {
	private final int rowIdx;
	private final T excelVO;

	ExcelRow(int rowIdx, Class<T> clazz) {
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
