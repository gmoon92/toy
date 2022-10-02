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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumConverterFactory implements ConverterFactory<String, Enum<?>> {

	private static final Map<Class, Converter> CACHE = new ConcurrentHashMap<>();

	@Override
	public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetClass) {
		if (CACHE.get(targetClass) == null) {
			log.info("targetClass: {}", targetClass);
			CACHE.put(targetClass, StringToEnumFactory.create(targetClass));
		}

		return CACHE.get(targetClass);
	}

	private static class StringToEnumFactory {

		static Converter create(Class<? extends Enum<?>> targetClass) {
			boolean isCustomBinder = stream(targetClass.getInterfaces())
				.anyMatch(i -> i == StringToEnumBinder.class);
			if (isCustomBinder) {
				return new CustomStringToEnum(targetClass);
			}

			return new DefaultStringToEnum(targetClass);
		}

		@RequiredArgsConstructor
		static class DefaultStringToEnum implements Converter<String, Enum> {

			private final Class<? extends Enum> targetClass;

			@Override
			public Enum convert(String source) {
				return Enum.valueOf(targetClass, source);
			}
		}

		static class CustomStringToEnum implements Converter<String, Enum<? extends StringToEnumBinder>> {

			private final Map<String, Enum> binder;

			private CustomStringToEnum(Class<? extends Enum> targetClass) {
				binder = stream(targetClass.getEnumConstants())
					.collect(collectingAndThen(
						toMap(o -> ((StringToEnumBinder)o).getValue(), Function.identity()),
						Collections::unmodifiableMap
					));
			}

			@Override
			public Enum<? extends StringToEnumBinder> convert(String source) {
				Enum<? extends StringToEnumBinder> result = binder.get(source);
				if (result == null) {
					throw new ConversionFailedException();
				}

				return result;
			}
		}
	}

	public int size() {
		return CACHE.size();
	}
}
