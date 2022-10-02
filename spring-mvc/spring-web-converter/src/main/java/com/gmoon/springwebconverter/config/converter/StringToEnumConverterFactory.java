package com.gmoon.springwebconverter.config.converter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<? extends StringToEnumConverter>> {

	@Autowired
	private List<StringToEnumConverter> converters;

	@Override
	public <T extends Enum<? extends StringToEnumConverter>> Converter<String, T> getConverter(Class<T> targetType) {

		return null;
	}

	public List<StringToEnumConverter> getConverters() {
		return converters;
	}
}
