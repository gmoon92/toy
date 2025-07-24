package com.gmoon.springpoi.base.persistence.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IntegerSetConverter implements AttributeConverter<List<Integer>, String> {
	private static final String DELIMITER = ",";

	@Override
	public String convertToDatabaseColumn(List<Integer> attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return "";
		}
		return attribute.stream()
			 .filter(Objects::nonNull)
			 .map(String::valueOf)
			 .collect(Collectors.joining(DELIMITER));
	}

	@Override
	public List<Integer> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return Collections.emptyList();
		}

		return Arrays.stream(dbData.split(DELIMITER))
			 .filter(s -> !s.isEmpty())
			 .map(Integer::valueOf)
			 .toList();
	}
}
