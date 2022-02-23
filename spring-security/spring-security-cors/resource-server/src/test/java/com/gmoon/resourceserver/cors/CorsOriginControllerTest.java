package com.gmoon.resourceserver.cors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.Charset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.gmoon.resourceserver.test.BaseIntegrationTest;
import com.gmoon.resourceserver.util.JacksonUtils;

class CorsOriginControllerTest extends BaseIntegrationTest {
	@BeforeEach
	void setUp() {
		setupMockMvc();
	}

	@Test
	void testGetAll() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/cors")
			.header(HttpHeaders.AUTHORIZATION, TOKEN_OF_ADMIN)
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
			.header(HttpHeaders.AUTHORIZATION, TOKEN_OF_ADMIN)
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
			.header(HttpHeaders.AUTHORIZATION, TOKEN_OF_ADMIN)
			.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isNoContent());
	}
}
