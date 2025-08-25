package com.gmoon.springpoi.base.persistence.converter;

import com.gmoon.springpoi.excels.domain.vo.ExcelCellErrorMessages;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ExcelCellErrorMessageConverter implements AttributeConverter<ExcelCellErrorMessages, String> {

	@Override
	public String convertToDatabaseColumn(ExcelCellErrorMessages attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.toJsonString();
	}

	@Override
	public ExcelCellErrorMessages convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return new ExcelCellErrorMessages(dbData);
		}
		return new ExcelCellErrorMessages(dbData);
	}
}
