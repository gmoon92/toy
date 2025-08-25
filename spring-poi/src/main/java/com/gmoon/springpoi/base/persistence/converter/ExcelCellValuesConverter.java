package com.gmoon.springpoi.base.persistence.converter;

import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ExcelCellValuesConverter implements AttributeConverter<ExcelCellValues, String> {

	@Override
	public String convertToDatabaseColumn(ExcelCellValues attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.toJsonString();
	}

	@Override
	public ExcelCellValues convertToEntityAttribute(String dbData) {
		return new ExcelCellValues(dbData);
	}
}

