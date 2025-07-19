package com.gmoon.commons.commonsapachepoi.excel.validator.common;

import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelValidator;

@ExcelComponent
public class BooleanStringYNValidator implements ExcelValidator {

	@Override
	public boolean isValid(String value) {
		return "Y".equals(value) || "N".equals(value);
	}
}
