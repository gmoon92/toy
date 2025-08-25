package com.gmoon.springpoi.common.excel.validator.users;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;
import com.gmoon.springpoi.common.excel.vo.ExcelErrorMessage;
import com.gmoon.springpoi.common.utils.ValidationUtil;

@ExcelComponent
public class UsernameValidator implements ExcelValidator {
	private final Pattern pattern;

	public UsernameValidator(@Value("${service.validation.regex.username}") String regex) {
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean isValid(String username) {
		return StringUtils.isNotBlank(username)
			 && ValidationUtil.isRange(username.length(), 4, 24)
			 && pattern.matcher(username).matches();
	}

	@Override
	public ExcelErrorMessage getErrorMessage() {
		return new ExcelErrorMessage("사용자 아이디는 숫자와, 대소문자를 포함한 4~24자만 입력 가능합니다.");
	}
}
