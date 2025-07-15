package com.gmoon.commons.commonsapachepoi.excel.validator.users;

import com.gmoon.commons.commonsapachepoi.common.utils.SecurityUtil;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelEnumValidator;
import com.gmoon.commons.commonsapachepoi.users.Role;
import com.gmoon.commons.commonsapachepoi.users.User;

public class UserRoleValidator extends ExcelEnumValidator<Role> {

	protected UserRoleValidator() {
		super(Role.class);
	}

	@Override
	public boolean isValid(Role role) {
		try {
			User user = SecurityUtil.getCurrentUser();
			return user.isAdmin()
				 && Role.USER == role;
		} catch (Exception e) {
			return false;
		}
	}
}
