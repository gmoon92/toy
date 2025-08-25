package com.gmoon.springpoi.annotation;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.gmoon.springpoi.common.excel.converter.ExcelConverter;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;

@SpringBootTest
class ExcelComponentTest {

	@Autowired
	private List<ExcelConverter<?>> converters;
	@Autowired
	private List<ExcelValidator> validators;
	@Autowired
	private ApplicationContext context;

	@DisplayName("검증기와 컨버터 빈은 프로토타입으로 생성된다.")
	@Test
	void testPrototypeBeans() {
		Stream.concat(converters.stream(), validators.stream())
			 .map(Object::getClass)
			 .forEach(clazz ->
				  assertThat(context.getBean(clazz))
					   .as("prototype")
					   .isNotSameAs(context.getBean(clazz))
			 );
	}
}
