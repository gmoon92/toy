package com.gmoon.commons.commonsapachepoi.excel.predicate;

import com.gmoon.commons.commonsapachepoi.common.security.SecurityUtil;
import com.gmoon.commons.commonsapachepoi.users.Role;
import com.gmoon.commons.commonsapachepoi.users.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRoleValidator implements ExcelValidator {

	@Override
	public boolean isValid(String authority) {
		try {
			User user = SecurityUtil.getCurrentUser();
			return user.isAdmin()
				 && Role.USER == Role.valueOf(authority);
		} catch (Exception e) {
			return false;
		}
	}
}
