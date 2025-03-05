package com.gmoon.payment.appstore.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.gmoon.payment.global.exception.ErrorCode;
import com.gmoon.payment.test.Fixtures;
import com.gmoon.payment.test.IntegrationTestCase;

@EnabledIf(value = "#{'${service.payment.appstore.enabled}' == '1'}", loadContext = true)
@TestPropertySource(properties = {"service.payment.appstore.enabled=0"})
@IntegrationTestCase
class AppStoreControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@DisplayName("결제 데이터 조회")
	@Nested
	class GetTransactionInfoTest {

		@Test
		void success() throws Exception {
			ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/appstore/" + Fixtures.AppStore.TRANSACTION_ID)
				 .accept(MediaType.APPLICATION_JSON)
				 .contentType(MediaType.APPLICATION_JSON)
			);

			result.andExpect(status().isOk());
			result.andExpect(jsonPath("$.transactionId").isNotEmpty());
			result.andExpect(jsonPath("$.orgTransactionId").isNotEmpty());
			result.andExpect(jsonPath("$.price").isNotEmpty());
			result.andExpect(jsonPath("$.purchaseTime").isNotEmpty());
		}

		@DisplayName("잘못된 결제 키")
		@Test
		void error() throws Exception {
			ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/appstore/20000008380691981")
				 .accept(MediaType.APPLICATION_JSON)
				 .contentType(MediaType.APPLICATION_JSON)
			);

			result.andExpect(status().is4xxClientError());
			result.andExpect(jsonPath("$.exceptionCode").value(ErrorCode.APP_STORE_CLIENT_ERROR));
			result.andExpect(jsonPath("$.exceptionDetailCode").value(ErrorCode.APP_STORE_API_REQUEST_ERROR));
			result.andExpect(jsonPath("$.apiExceptionCode").isNotEmpty());
			result.andExpect(jsonPath("$.apiExceptionDetailCode").isNotEmpty());
			result.andExpect(jsonPath("$.apiExceptionMessage").isNotEmpty());
		}
	}
}
