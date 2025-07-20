package com.gmoon.springpoi.excel.validator.users;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.springpoi.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.excel.converter.common.StringToIntegerConverter;
import com.gmoon.springpoi.excel.validator.ExcelValidator;

import lombok.RequiredArgsConstructor;

@ExcelComponent
@RequiredArgsConstructor
public class UseGenderValidator implements ExcelValidator {
	private final StringToIntegerConverter converter;

	@Override
	public boolean isValid(String gender) {
		if (StringUtils.isBlank(gender)) {
			return false;
		}

		int code = converter.convert(gender);
		return code == 0 || code == 1;
	}
}
