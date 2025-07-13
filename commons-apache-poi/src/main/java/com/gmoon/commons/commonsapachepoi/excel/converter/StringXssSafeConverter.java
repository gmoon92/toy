package com.gmoon.commons.commonsapachepoi.excel.converter;

import java.util.Objects;

import com.gmoon.commons.commonsapachepoi.common.utils.XssUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;

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
