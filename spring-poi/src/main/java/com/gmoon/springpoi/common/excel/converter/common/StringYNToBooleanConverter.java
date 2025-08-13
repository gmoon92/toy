package com.gmoon.springpoi.common.excel.converter.common;

import org.apache.commons.lang3.BooleanUtils;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.converter.ExcelConverter;

@ExcelComponent
public class StringYNToBooleanConverter implements ExcelConverter<Boolean> {

	@Override
	public Boolean convert(String value) {
		return ("Y".equals(value) || "N".equals(value))
			 && BooleanUtils.toBoolean(value);
	}
}
