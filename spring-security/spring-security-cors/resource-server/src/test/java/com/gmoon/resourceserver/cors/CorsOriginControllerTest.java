package com.gmoon.resourceserver.cors;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.Charset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gmoon.resourceserver.util.JacksonUtils;

@SpringBootTest
class CorsOriginControllerTest {
	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.alwaysDo(print())
			.apply(springSecurity())
			.build();
	}

	@Test
	void testGetAll() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/cors")
			.accept(MediaType.APPLICATION_JSON)
			.characterEncoding(Charset.defaultCharset()));

		// then
		result.andExpect(status().isOk());
	}

	@Test
	void testSave() throws Exception {
		// given
		Origin origin = Origin.builder()
			.schema("http")
			.host("localhost")
			.port(8080)
			.build();

		// when
		ResultActions result = mockMvc.perform(post("/cors")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(JacksonUtils.toString(origin)));

		// then
		result.andExpect(status().isCreated());
	}

	@Test
	void testRemove() throws Exception {
		// given
		long id = 1;

		// when
		ResultActions result = mockMvc.perform(delete("/cors/" + id)
			.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isNoContent());
	}
}
