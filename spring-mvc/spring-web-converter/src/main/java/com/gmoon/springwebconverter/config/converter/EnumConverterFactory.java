package com.gmoon.springwebconverter.config.converter;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import com.gmoon.springwebconverter.config.error.exception.ConversionFailedException;

public class EnumConverterFactory implements ConverterFactory<Object, Enum<? extends ValueToEnumBinder>> {

	private static final Map<Class, Converter> CACHE = new ConcurrentHashMap<>();

	@Override
	public <T extends Enum<? extends ValueToEnumBinder>> Converter<Object, T> getConverter(Class<T> targetClass) {
		if (CACHE.get(targetClass) == null) {
			CACHE.put(targetClass, new EnumConverter(targetClass));
		}

		return CACHE.get(targetClass);
	}

	private static class EnumConverter implements Converter<Object, Enum<? extends ValueToEnumBinder>> {

		private final Map<Object, Enum<? extends ValueToEnumBinder>> ALL;

		private EnumConverter(Class<? extends Enum<? extends ValueToEnumBinder>> targetClass) {
			ALL = stream(targetClass.getEnumConstants())
				.collect(collectingAndThen(
					toMap(o -> ((ValueToEnumBinder)o).getValue(), Function.identity()),
					Collections::unmodifiableMap
				));
		}

		@Override
		public Enum<? extends ValueToEnumBinder> convert(Object source) {
			Enum<? extends ValueToEnumBinder> result = ALL.get(source);
			if (result == null) {
				throw new ConversionFailedException();
			}

			return result;
		}
	}

	public int size() {
		return CACHE.size();
	}
}
