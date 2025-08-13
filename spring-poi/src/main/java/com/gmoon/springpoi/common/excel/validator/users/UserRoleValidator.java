package com.gmoon.springpoi.common.excel.validator.users;

import java.util.EnumSet;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.validator.ExcelEnumValidator;
import com.gmoon.springpoi.common.utils.SecurityUtil;
import com.gmoon.springpoi.users.domain.Role;
import com.gmoon.springpoi.users.domain.User;

@ExcelComponent
public class UserRoleValidator extends ExcelEnumValidator<Role> {

	protected UserRoleValidator() {
		super(Role.class);
	}

	@Override
	public boolean isValid(Role role) {
		if (Role.ADMIN == role) {
			return false;
		}

		User user = SecurityUtil.getCurrentUser();
		return user.isAdmin()
			 && EnumSet.of(Role.MANAGER, Role.USER).contains(role);
	}
}
