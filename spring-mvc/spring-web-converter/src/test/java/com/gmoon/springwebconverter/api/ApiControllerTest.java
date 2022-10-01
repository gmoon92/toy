package com.gmoon.springwebconverter.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

	@DisplayName("요청 문자열 커스텀 Enum Converter 매핑 검증")
	@ParameterizedTest
	@EnumSource(SearchType.class)
	void enumStringParams(SearchType searchType) throws Exception {
		ResultActions result = mockMvc.perform(get("/api")
			.accept(MediaType.APPLICATION_JSON)
			.param("searchType", searchType.getValue())
		);

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$").value(searchType.name()));
	}
}
