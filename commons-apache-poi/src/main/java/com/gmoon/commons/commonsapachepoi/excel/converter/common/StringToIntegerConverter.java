package com.gmoon.commons.commonsapachepoi.excel.converter.common;

import org.apache.commons.lang3.math.NumberUtils;

import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.converter.ExcelConverter;

@ExcelComponent
public class StringToIntegerConverter implements ExcelConverter<Integer> {

	@Override
	public Integer convert(String value) {
		try {
			return NumberUtils.toInt(value);
		} catch (Exception e) {
			return 0;
		}
	}
}
