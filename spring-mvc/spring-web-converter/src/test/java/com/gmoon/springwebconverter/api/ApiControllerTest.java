package com.gmoon.springwebconverter.api;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.gmoon.springwebconverter.model.SearchType;

@SpringBootTest
class ApiControllerTest {

	@Autowired
	WebApplicationContext wac;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac)
			.alwaysDo(print())
			.build();
	}

	@DisplayName("요청 문자열을 Enum 타입으로 변환한다.")
	@ParameterizedTest
	@EnumSource(SearchType.class)
	void convertStringToEnum(SearchType searchType) throws Exception {
		ResultActions result = mockMvc.perform(get("/api")
			.accept(MediaType.APPLICATION_JSON)
			.param("searchType", searchType.getValue())
		);

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$").value(searchType.name()));
	}

	@DisplayName("요청 문자열을 Enum 타입으로 변환하지 못할 경우 예외가 발생한다.")
	@Test
	void error() throws Exception {
		ResultActions result = mockMvc.perform(get("/api")
			.accept(MediaType.APPLICATION_JSON)
			.param("searchType", "null")
		);

		result.andExpect(status().isBadRequest());
		result.andExpect(matcher ->
			assertThat(matcher.getResolvedException())
				.isInstanceOf(MethodArgumentTypeMismatchException.class)
				.hasMessageContaining("ConversionFailedException")
				.hasMessageContaining("요청한 문자열을 이넘 클래스로 변환할 수 없습니다.")
		);
	}
}