package com.gmoon.springpoi.excel.validator.common;

import com.gmoon.springpoi.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.excel.validator.ExcelValidator;

@ExcelComponent
public class BooleanStringYNValidator implements ExcelValidator {

	@Override
	public boolean isValid(String value) {
		return "Y".equals(value) || "N".equals(value);
	}
}
