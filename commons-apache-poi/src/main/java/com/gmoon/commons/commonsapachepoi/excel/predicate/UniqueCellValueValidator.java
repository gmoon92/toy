package com.gmoon.commons.commonsapachepoi.excel.predicate;

import java.util.ArrayList;
import java.util.List;

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
