package com.gmoon.commons.commonsapachepoi.excel.predicate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BooleanStringYNValidator implements ExcelValidator {

	@Override
	public boolean isValid(String value) {
		return "Y".equals(value) || "N".equals(value);
	}
}
