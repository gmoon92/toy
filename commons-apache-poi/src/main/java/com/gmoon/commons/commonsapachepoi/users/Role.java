package com.gmoon.commons.commonsapachepoi.users;

import org.springframework.security.core.GrantedAuthority;

import com.gmoon.commons.commonsapachepoi.excel.provider.ExcelValueProvider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role implements GrantedAuthority, ExcelValueProvider {
	ADMIN("ROLE_ADMIN"),
	MANAGER("ROLE_MANAGER"),
	USER("ROLE_USER");

	private final String authority;

	@Override
	public String getExcelCellValue() {
		return name();
	}
}
