package com.gmoon.springpoi.annotation;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.gmoon.springpoi.common.excel.converter.users.StringToRoleConverter;
import com.gmoon.springpoi.common.excel.validator.common.UniqueCellValueValidator;

@SpringBootTest
class ExcelComponentTest {

	@Autowired
	private ApplicationContext context;

	@Test
	void test() {
		// singleton
		assertThat(context.getBean(StringToRoleConverter.class)).isSameAs(context.getBean(StringToRoleConverter.class));
		assertThat(context.getBean(StringToRoleConverter.class)).isSameAs(context.getBean(StringToRoleConverter.class));

		// prototype
		assertThat(context.getBean(UniqueCellValueValidator.class))
			 .isNotSameAs(context.getBean(UniqueCellValueValidator.class));
	}
}
