package com.gmoon.springpoi.users.domain;

import org.springframework.security.core.GrantedAuthority;

import com.gmoon.springpoi.excel.provider.ExcelValueProvider;

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
