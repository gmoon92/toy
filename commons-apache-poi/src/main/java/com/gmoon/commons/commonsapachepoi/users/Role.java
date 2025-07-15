package com.gmoon.commons.commonsapachepoi.users;

import org.springframework.security.core.GrantedAuthority;

import com.gmoon.commons.commonsapachepoi.common.base.CodeBasedEnum;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelValueProvider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role implements GrantedAuthority, CodeBasedEnum<String>, ExcelValueProvider {
	ADMIN("ROLE_ADMIN"),
	MANAGER("ROLE_MANAGER"),
	USER("ROLE_USER");

	private final String authority;

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String getExcelCellValue() {
		return name();
	}
}
