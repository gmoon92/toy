package com.gmoon.commons.commonsapachepoi.excel.predicate;

import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LineBreakNotAllowedValidator implements ExcelValidator {

	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\r\n|\n|\r");

	@Override
	public boolean isValid(String cellValue) {
		return cellValue != null
			 && !NEWLINE_PATTERN.matcher(cellValue).find();
	}
}
