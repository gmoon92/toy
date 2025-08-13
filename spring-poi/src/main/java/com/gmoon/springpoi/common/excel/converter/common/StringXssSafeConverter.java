package com.gmoon.springpoi.common.excel.converter.common;

import java.util.Objects;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.converter.ExcelConverter;
import com.gmoon.springpoi.common.utils.XssUtil;

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
