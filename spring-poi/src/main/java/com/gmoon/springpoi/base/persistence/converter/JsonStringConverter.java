package com.gmoon.springpoi.base.persistence.converter;

import com.gmoon.springpoi.base.persistence.vo.JsonString;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonStringConverter implements AttributeConverter<JsonString, String> {

	@Override
	public String convertToDatabaseColumn(JsonString attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return null;
		}

		return attribute.toJsonString();
	}

	@Override
	public JsonString convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return new JsonString();
		}
		return new JsonString(dbData);
	}
}

