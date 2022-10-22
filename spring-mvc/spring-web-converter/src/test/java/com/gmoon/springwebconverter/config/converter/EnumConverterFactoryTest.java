package com.gmoon.springwebconverter.config.converter;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.convert.converter.Converter;

import com.gmoon.springwebconverter.model.PaymentType;
import com.gmoon.springwebconverter.model.SearchType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class EnumConverterFactoryTest {

	static EnumConverterFactory factory;

	@BeforeAll
	static void beforeAll() {
		factory = new EnumConverterFactory();
	}

	@DisplayName("컨버터 반환")
	@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
	@MethodSource("enumClassProvider")
	void getConverter(Class enumClass, StringToEnumBinder binder) {
		Converter converter = factory.getConverter(enumClass);

		Object value = binder.getValue();
		Object expected = converter.convert(value);

		assertThat(expected).isNotNull();
		log.info("value: {}, expected: {}", value, expected);
	}

	static Stream<Arguments> enumClassProvider() {
		return Stream.of(
			Arguments.of(PaymentType.class, PaymentType.KAKAO_BANK),
			Arguments.of(SearchType.class, SearchType.USER_NAME)
		);
	}

	@AfterAll
	static void afterAll() {
		log.info("factory cache size: {}", factory.size());
		assertThat(factory.size()).isGreaterThanOrEqualTo(2);
	}
}
