package com.gmoon.springpoi.excel.validator;

import com.gmoon.springpoi.excel.converter.ExcelEnumConverter;
import com.gmoon.springpoi.excel.provider.ExcelValueProvider;

public abstract class ExcelEnumValidator<T extends Enum<T> & ExcelValueProvider>
	 extends ExcelEnumConverter<T> implements ExcelValidator {

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
