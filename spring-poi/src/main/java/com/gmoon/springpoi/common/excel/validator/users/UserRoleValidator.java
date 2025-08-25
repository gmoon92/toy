package com.gmoon.springpoi.common.excel.validator.users;

import java.util.EnumSet;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.validator.ExcelEnumValidator;
import com.gmoon.springpoi.common.excel.vo.ExcelErrorMessage;
import com.gmoon.springpoi.common.excel.vo.ExcelUploadContext;
import com.gmoon.springpoi.common.excel.vo.ExcelUploadContextHolder;
import com.gmoon.springpoi.users.domain.Role;

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

		ExcelUploadContext context = ExcelUploadContextHolder.getContext();
		return context.isAdminUser()
			 && EnumSet.of(Role.MANAGER, Role.USER).contains(role);
	}

	@Override
	public ExcelErrorMessage getErrorMessage() {
		return new ExcelErrorMessage("사용자 역할이 잘못 입력됐습니다.");
	}
}
