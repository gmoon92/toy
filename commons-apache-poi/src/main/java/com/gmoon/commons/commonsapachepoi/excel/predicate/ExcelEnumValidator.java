package com.gmoon.commons.commonsapachepoi.excel.predicate;

import com.gmoon.commons.commonsapachepoi.excel.converter.StringToEnumConverter;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelValueProvider;

public abstract class ExcelEnumValidator<T extends Enum<T> & ExcelValueProvider>
	 extends StringToEnumConverter<T>
	 implements ExcelValidator {

	protected ExcelEnumValidator(Class<T> enumClass) {
		super(enumClass);
	}

	public abstract boolean isValid(T enumValue);

	@Override
	public boolean isValid(String cellValue) {
		if (cellValue == null) {
			return false;
		}
		T enumValue = convert(cellValue);
		return enumValue != null
			 && isValid(enumValue);
	}
}
