package com.gmoon.commons.commonsapachepoi.excel.validator.common;

import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelValidator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BooleanStringYNValidator implements ExcelValidator {

	@Override
	public boolean isValid(String value) {
		return "Y".equals(value) || "N".equals(value);
	}
}
