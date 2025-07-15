package com.gmoon.commons.commonsapachepoi.excel.validator.common;

import java.util.ArrayList;
import java.util.List;

import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelValidator;

public class UniqueCellValueValidator implements ExcelValidator {
	private final List<String> values = new ArrayList<>();

	@Override
	public boolean isValid(String cellValue) {
		if (values.contains(cellValue)) {
			return false;
		}

		values.add(cellValue);
		return true;
	}
}
