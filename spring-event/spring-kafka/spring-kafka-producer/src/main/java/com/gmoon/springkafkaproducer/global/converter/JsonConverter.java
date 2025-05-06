package com.gmoon.springkafkaproducer.global.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonConverter implements AttributeConverter<Object, String> {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(Object attribute) {
		if (attribute == null) {
			return null;
		}

		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (Exception e) {
			throw new RuntimeException("Error converting object to JSON", e);
		}
	}

	@Override
	public Object convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}

		try {
			return objectMapper.readValue(dbData, Object.class);
		} catch (Exception e) {
			throw new RuntimeException("Error converting JSON to object", e);
		}
	}
}
