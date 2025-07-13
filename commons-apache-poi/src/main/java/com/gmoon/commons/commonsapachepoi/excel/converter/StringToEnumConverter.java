package com.gmoon.commons.commonsapachepoi.excel.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.commons.commonsapachepoi.common.base.CodeBasedEnum;

public abstract class StringToEnumConverter<T extends Enum<T> & CodeBasedEnum<String>>
	 implements ExcelConverter<T> {

	private final Map<String, T> codes;

	StringToEnumConverter(Class<T> enumClass) {
		this.codes = Arrays.stream(enumClass.getEnumConstants())
			 .collect(Collectors.collectingAndThen(
				  Collectors.toMap(CodeBasedEnum::getCode, Function.identity()),
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
