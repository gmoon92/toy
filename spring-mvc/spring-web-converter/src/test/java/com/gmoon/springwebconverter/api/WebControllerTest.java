package com.gmoon.springwebconverter.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.gmoon.springwebconverter.config.constants.HttpSessionAttributeKeys;

@DefaultTimeZone("UTC")
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class WebControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Nested
	class GenericConverterTest {
		@Test
		void param() throws Exception {
			ResultActions result = mockMvc.perform(get("/web/converter/param")
				 .sessionAttr(HttpSessionAttributeKeys.USER_TIME_ZONE, "Asia/Seoul")
				 .sessionAttr(HttpSessionAttributeKeys.USER_DATE_PATTERN, "yyyy-MM-dd")
				 .accept(MediaType.APPLICATION_JSON)
				 .contentType(MediaType.APPLICATION_JSON)
				 .param("wallTime", "2025-01-02")
			);

			result.andExpect(status().isOk());
			result.andExpect(jsonPath("$").value("2025-01-01T15:00:00.000+00:00"));
		}

		@Test
		void body() throws Exception {
			ResultActions result = mockMvc.perform(post("/web/converter/body")
				 .sessionAttr(HttpSessionAttributeKeys.USER_TIME_ZONE, "Asia/Seoul")
				 .sessionAttr(HttpSessionAttributeKeys.USER_DATE_PATTERN, "yyyy-MM-dd")
				 .accept(MediaType.APPLICATION_JSON)
				 .contentType(MediaType.APPLICATION_JSON)
				 .content("{\n"
					  + "  \"search\": {\n"
					  + "    \"keyword\": \"spring\",\n"
					  // + "    \"startDate\": \"2025-01-02T00:00:00.000+09:00\",\n"
					  // + "    \"endDate\": \"2025-01-31T23:59:59.000+09:00\"\n"
					  + "    \"startDate\": \"2025-01-02\",\n"
					  + "    \"endDate\": \"2025-01-31\"\n"
					  + "  }\n"
					  + "}")
			);

			result.andExpect(status().isOk());
			jsonPath("$.search.keyword").value("spring");
			jsonPath("$.search.startDate").value("2025-01-01T15:00:00.000+00:00");
			jsonPath("$.search.endDate").value("2025-01-31T14:59:59.000+00:00");
		}

		@Test
		void modelAndView() throws Exception {
			ResultActions result = mockMvc.perform(post("/web/converter/modelAndView")
				 .sessionAttr(HttpSessionAttributeKeys.USER_TIME_ZONE, "Asia/Seoul")
				 .sessionAttr(HttpSessionAttributeKeys.USER_DATE_PATTERN, "yyyy-MM-dd")
				 .accept(MediaType.APPLICATION_JSON)
				 .contentType(MediaType.APPLICATION_FORM_URLENCODED)
				 .param("search.keyword", "spring")
				 .param("search.startDate", "2025-01-02")
				 .param("search.endDate", "2025-01-31")
			);

			result.andExpect(status().isOk());
			jsonPath("$.search.keyword").value("spring");
			jsonPath("$.search.startDate").value("2025-01-01T15:00:00.000+00:00");
			jsonPath("$.search.endDate").value("2025-01-31T14:59:59.000+00:00");
		}
	}

}
