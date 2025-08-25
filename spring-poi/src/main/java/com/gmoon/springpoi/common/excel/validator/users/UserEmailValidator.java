package com.gmoon.springpoi.common.excel.validator.users;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;
import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;
import com.gmoon.springpoi.common.excel.vo.ExcelErrorMessage;
import com.gmoon.springpoi.common.utils.ValidationUtil;

@ExcelComponent
public class UserEmailValidator implements ExcelValidator {
	private final Pattern pattern;

	public UserEmailValidator(@Value("${service.validation.regex.email}") String regex) {
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean isValid(String email) {
		return StringUtils.isNotBlank(email)
			 && ValidationUtil.isRange(email.length(), 1, ColumnLength.EMAIL)
			 && pattern.matcher(email).matches();
	}

	@Override
	public ExcelErrorMessage getErrorMessage() {
		return new ExcelErrorMessage("이메일을 다시 확인해주세요.");
	}
}
