package com.gmoon.springpoi.excel.converter.common;

import org.apache.commons.lang3.math.NumberUtils;

import com.gmoon.springpoi.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.excel.converter.ExcelConverter;

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
