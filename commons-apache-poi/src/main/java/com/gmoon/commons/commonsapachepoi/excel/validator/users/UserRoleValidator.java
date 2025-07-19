package com.gmoon.commons.commonsapachepoi.excel.validator.users;

import com.gmoon.commons.commonsapachepoi.common.utils.SecurityUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelEnumValidator;
import com.gmoon.commons.commonsapachepoi.users.domain.Role;
import com.gmoon.commons.commonsapachepoi.users.domain.User;

@ExcelComponent
public class UserRoleValidator extends ExcelEnumValidator<Role> {

	protected UserRoleValidator() {
		super(Role.class);
	}

	@Override
	public boolean isValid(Role role) {
		User user = SecurityUtil.getCurrentUser();
		return user.isAdmin()
			 && Role.USER == role;
	}
}
