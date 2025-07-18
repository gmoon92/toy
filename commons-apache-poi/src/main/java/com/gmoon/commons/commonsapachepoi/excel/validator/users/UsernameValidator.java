package com.gmoon.commons.commonsapachepoi.excel.validator.users;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.gmoon.commons.commonsapachepoi.common.utils.ValidationUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelValidator;

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
}
