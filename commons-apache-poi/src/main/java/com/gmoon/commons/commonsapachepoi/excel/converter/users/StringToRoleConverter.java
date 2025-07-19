package com.gmoon.commons.commonsapachepoi.excel.converter.users;

import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.converter.ExcelEnumConverter;
import com.gmoon.commons.commonsapachepoi.users.Role;

@ExcelComponent
public class StringToRoleConverter extends ExcelEnumConverter<Role> {

	public StringToRoleConverter() {
		super(Role.class);
	}
}
