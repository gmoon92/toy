package com.gmoon.springpoi.common.excel.validator.common;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;
import com.gmoon.springpoi.common.excel.vo.ExcelErrorMessage;

@ExcelComponent
public class BooleanStringYNValidator implements ExcelValidator {

	@Override
	public boolean isValid(String value) {
		return "Y".equals(value) || "N".equals(value);
	}

	@Override
	public ExcelErrorMessage getErrorMessage() {
		return new ExcelErrorMessage("'Y', 'N' 문자열만 입력 가능합니다.");
	}
}
