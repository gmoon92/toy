package com.gmoon.springpoi.excel.converter.common;

import java.util.Objects;

import com.gmoon.springpoi.common.utils.XssUtil;
import com.gmoon.springpoi.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.excel.converter.ExcelConverter;

@ExcelComponent
public class StringXssSafeConverter implements ExcelConverter<String> {

	@Override
	public String convert(String value) {
		if (Objects.isNull(value)) {
			return "";
		}

		return XssUtil.getCleanHTML(value);
	}
}
