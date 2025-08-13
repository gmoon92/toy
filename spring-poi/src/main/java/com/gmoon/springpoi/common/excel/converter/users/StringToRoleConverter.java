package com.gmoon.springpoi.common.excel.converter.users;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.converter.ExcelEnumConverter;
import com.gmoon.springpoi.users.domain.Role;

@ExcelComponent
public class StringToRoleConverter extends ExcelEnumConverter<Role> {

	public StringToRoleConverter() {
		super(Role.class);
	}
}
