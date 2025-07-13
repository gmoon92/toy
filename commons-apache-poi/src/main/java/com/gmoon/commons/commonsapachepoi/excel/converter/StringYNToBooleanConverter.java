package com.gmoon.commons.commonsapachepoi.excel.converter;

import org.apache.commons.lang3.BooleanUtils;

import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;

@ExcelComponent
public class StringYNToBooleanConverter implements ExcelConverter<Boolean> {

	@Override
	public Boolean convert(String value) {
		return ("Y".equals(value) || "N".equals(value))
			 && BooleanUtils.toBoolean(value);
	}
}
