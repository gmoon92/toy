package com.gmoon.commons.commonsapachepoi.excel.converter.common;

import java.util.Objects;

import com.gmoon.commons.commonsapachepoi.common.utils.XssUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.converter.ExcelConverter;

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
