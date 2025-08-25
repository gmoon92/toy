package com.gmoon.springpoi.common.excel.validator.common;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.springpoi.common.excel.validator.ExcelValidator;
import com.gmoon.springpoi.common.excel.vo.ExcelErrorMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequiredValueValidator implements ExcelValidator {
	public static final RequiredValueValidator INSTANCE = new RequiredValueValidator();

	@Override
	public boolean isValid(String value) {
		if (value == null) {
			return false;
		}

		return StringUtils.isNotBlank(value.trim());
	}

	@Override
	public ExcelErrorMessage getErrorMessage() {
		return new ExcelErrorMessage("필수 입력 항목입니다.");
	}
}
