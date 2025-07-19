package com.gmoon.commons.commonsapachepoi.excel.validator.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;

import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelValidator;

@ExcelComponent(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
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
