package com.gmoon.springpoi.excel.converter.users;

import com.gmoon.springpoi.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.excel.converter.ExcelEnumConverter;
import com.gmoon.springpoi.users.domain.Role;

@ExcelComponent
public class StringToRoleConverter extends ExcelEnumConverter<Role> {

	public StringToRoleConverter() {
		super(Role.class);
	}
}
