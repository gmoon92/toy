package com.gmoon.springwebconverter.model;

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.gmoon.springwebconverter.config.converter.StringToEnumBinder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SearchType implements StringToEnumBinder {

	DATE("date"),
	USER_NAME("username");

	private final String value;

	private static final Map<String, SearchType> ALL = Arrays.stream(values())
		 .collect(collectingAndThen(
			  toMap(SearchType::getValue, Function.identity()),
			  Collections::unmodifiableMap
		 ));

	public static SearchType from(String value) {
		return ALL.get(value);
	}
}
