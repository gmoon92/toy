package com.gmoon.commons.commonsapachepoi.excel.vo;

import java.lang.reflect.Field;

import com.gmoon.commons.commonsapachepoi.common.utils.ReflectionUtil;
import com.gmoon.commons.commonsapachepoi.excel.converter.ExcelConverter;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ExcelRow<T> {
	private final int rowIdx;
	private final T excelVO;
	private boolean valid;

	public ExcelRow(int rowIdx, Class<T> clazz) {
		this.rowIdx = rowIdx;
		this.excelVO = ReflectionUtil.newInstance(clazz);
		this.valid = true;
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

	public void invalidate() {
		this.valid = false;
	}
}
