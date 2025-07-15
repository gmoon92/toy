package com.gmoon.commons.commonsapachepoi.excel.converter.users;

import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.converter.BaseEnumConverter;
import com.gmoon.commons.commonsapachepoi.users.Role;

@ExcelComponent
public class StringToRoleConverter extends BaseEnumConverter<Role> {

	public StringToRoleConverter() {
		super(Role.class);
	}
}
