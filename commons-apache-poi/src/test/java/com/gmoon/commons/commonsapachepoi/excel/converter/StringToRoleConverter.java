package com.gmoon.commons.commonsapachepoi.excel.converter;

import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.users.Role;

@ExcelComponent
public class StringToRoleConverter extends StringToEnumConverter<Role> {

	public StringToRoleConverter() {
		super(Role.class);
	}
}
