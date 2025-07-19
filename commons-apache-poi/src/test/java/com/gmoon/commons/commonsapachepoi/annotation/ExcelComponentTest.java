package com.gmoon.commons.commonsapachepoi.annotation;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.gmoon.commons.commonsapachepoi.excel.converter.users.StringToRoleConverter;
import com.gmoon.commons.commonsapachepoi.excel.validator.common.UniqueCellValueValidator;

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
