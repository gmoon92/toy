package com.gmoon.springpoi.common.excel.validator.common;

import java.util.regex.Pattern;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;

@ExcelComponent
public class LineBreakNotAllowedValidator implements ExcelValidator {
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\r\n|\n|\r");

	@Override
	public boolean isValid(String cellValue) {
		return cellValue != null
			 && !NEWLINE_PATTERN.matcher(cellValue).find();
	}
}
