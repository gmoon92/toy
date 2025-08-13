package com.gmoon.springpoi.common.excel.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.springpoi.common.excel.provider.ExcelValueProvider;

public abstract class ExcelEnumConverter<T extends Enum<T> & ExcelValueProvider>
	 implements ExcelConverter<T> {
	private final Map<String, T> codes;

	protected ExcelEnumConverter(Class<T> enumClass) {
		this.codes = Arrays.stream(enumClass.getEnumConstants())
			 .collect(Collectors.collectingAndThen(
				  Collectors.toMap(ExcelValueProvider::getExcelCellValue, Function.identity()),
				  Collections::unmodifiableMap
			 ));
	}

	@Override
	public T convert(String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}

		return codes.get(value);
	}
}
