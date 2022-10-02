package com.gmoon.springwebconverter.config.converter;

import org.springframework.core.convert.converter.Converter;

import com.gmoon.springwebconverter.config.error.exception.ConversionFailedException;
import com.gmoon.springwebconverter.model.SearchType;

public class SearchTypeConverter implements Converter<String, SearchType> {

	@Override
	public SearchType convert(String source) {
		SearchType result = SearchType.from(source);
		if (result == null) {
			throw new ConversionFailedException();
		}

		return result;
	}
}
