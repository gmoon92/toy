package com.gmoon.commons.commonsapachepoi.excel.validator.users;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.gmoon.commons.commonsapachepoi.common.utils.ValidationUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelValidator;

@ExcelComponent
public class UserEmailValidator implements ExcelValidator {
	;

	private final Pattern pattern;

	public UserEmailValidator(@Value("${validation.regex.email}") String regex) {
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean isValid(String email) {
		return StringUtils.isNotBlank(email)
			 && ValidationUtil.isRange(email.length(), 1, 50)
			 && pattern.matcher(email).matches();
	}
}
