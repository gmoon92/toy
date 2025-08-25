package com.gmoon.springpoi.common.excel.validator.users;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.converter.common.StringToIntegerConverter;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;
import com.gmoon.springpoi.common.excel.vo.ExcelErrorMessage;

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

	@Override
	public ExcelErrorMessage getErrorMessage() {
		return new ExcelErrorMessage("사용자 성별은 남자('0'), 여자('1')만 입력 가능합니다.");
	}
}
