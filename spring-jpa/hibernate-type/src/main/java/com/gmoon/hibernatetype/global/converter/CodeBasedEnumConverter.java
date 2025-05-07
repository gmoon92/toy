package com.gmoon.hibernatetype.global.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Converter
abstract class CodeBasedEnumConverter<T extends Enum<T> & CodeBasedEnum>
	 implements AttributeConverter<T, Integer> {

	private final Map<Integer, T> codes;

	CodeBasedEnumConverter(Class<T> enumClass) {
		this.codes = Arrays.stream(enumClass.getEnumConstants())
			 .collect(Collectors.collectingAndThen(
				  Collectors.toMap(CodeBasedEnum::getCode, Function.identity()),
				  Collections::unmodifiableMap
			 ));
	}

	@Override
	public Integer convertToDatabaseColumn(T attribute) {
		if (attribute == null) {
			return null;
		}

		return attribute.getCode();
	}

	@Override
	public T convertToEntityAttribute(Integer dbData) {
		if (dbData == null) {
			return null;
		}

		return codes.get(dbData);
	}
}
