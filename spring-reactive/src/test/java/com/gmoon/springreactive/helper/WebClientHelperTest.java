package com.gmoon.springreactive.helper;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * @apiNote Use <a href="https://fakerestapi.azurewebsites.net/index.html">FakeRestAPI</a> for network-communication verification
 *
 * @see WebTestClient
 */
@Disabled("외부 API에 의존하는 비결정적 테스트이므로 학습 목적 외에 사용하지 않는다.")
@SpringBootTest
class WebClientHelperTest {

	private static final String FAKE_REST_API_URI = "https://fakerestapi.azurewebsites.net/api/v1/Activities/";

	private final Payload payload = Payload.newInstance();

	@Autowired
	private WebClient webClient;

	@Autowired
	private WebClientHelper helper;

	record Payload(
		 int id,
		 String title,
		 String dueDate,
		 boolean completed
	) {

		public static Payload newInstance() {
			return new Payload(1, "title", "2022-06-13T08:30:02.337Z", true);
		}
	}

	@DisplayName("WebClientHelper 사용 시")
	@Nested
	class HelperTest {

		@DisplayName("GET 요청에 url 외에 선택적으로 header, queryParam 설정할 수 있다.")
		@Test
		void get() {
			var id = payload.id;

			var result = helper.get()
				 .uri(FAKE_REST_API_URI + id)
				 .bodyToMono(Payload.class)
				 .block();

			var assertions = new SoftAssertions();
			assertions.assertThat(result).isNotNull();
			assertions.assertThat(result.id).isEqualTo(id);
			assertions.assertAll();
		}

		@DisplayName("POST 요청에 url, body, 응답 클래스 외에 선택적으로 header 설정할 수 있다")
		@Test
		void post() {
			Map<String, String> headers = new HashMap<>();
			headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
			headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

			var result = helper.post()
				 .uri(FAKE_REST_API_URI)
				 .headers(headers)
				 .bodyValue(payload)
				 .bodyToMono(Payload.class)
				 .block();

			var assertions = new SoftAssertions();
			assertions.assertThat(result.id).isEqualTo(payload.id);
			assertions.assertThat(result.title).isEqualTo(payload.title);
			assertions.assertThat(result.completed).isEqualTo(payload.completed);
			assertions.assertAll();
		}

		@DisplayName("PUT 요청은 POST 요청과 유사하다")
		@Test
		void put() {
			Map<String, String> headers = new HashMap<>();

			var result = helper.put()
				 .uri(FAKE_REST_API_URI + payload.id)
				 .headers(headers)
				 .bodyValue(payload)
				 .bodyToMono(Payload.class)
				 .block();

			var assertions = new SoftAssertions();
			assertions.assertThat(payload.id).isEqualTo(result.id);
			assertions.assertThat(payload.title).isEqualTo(result.title);
			assertions.assertThat(payload.completed).isEqualTo(result.completed);
			assertions.assertAll();
		}

		@DisplayName("DELETE 요청에 선택적으로 응답 클래스를 추가해 응답을 받을 수 있으며 기본적으로는 void 반환한다")
		@Test
		void delete() {
			var id = payload.id;

			HttpStatusCode responseStatusCode = helper.delete()
				 .uri(FAKE_REST_API_URI + id)
				 .exchangeToMono(clientResponse -> Mono.just(clientResponse.statusCode()))
				 .block();

			assertThat(responseStatusCode).isEqualTo(HttpStatus.OK);
		}
	}

	@DisplayName("WebClient 사용 시")
	@Nested
	class WebClientTest {

		@DisplayName("GET 요청에 특별한 HEADER 설정이 필요 없다면 url, 응답 클래스만 설정하면 된다")
		@Test
		void get() {
			var id = payload.id;

			var result = webClient.get()
				 .uri(FAKE_REST_API_URI + id)
				 .retrieve()
				 .bodyToMono(Payload.class)
				 .block();

			var assertions = new SoftAssertions();
			assertions.assertThat(result).isNotNull();
			assertions.assertThat(result.id).isEqualTo(id);
			assertions.assertAll();
		}

		@DisplayName("POST 요청에 url, 응답 클래스 외에도 contentType, body 설정이 필요하다")
		@Test
		void post() {
			var result = webClient.post()
				 .uri(FAKE_REST_API_URI)
				 .accept(MediaType.APPLICATION_JSON)
				 .contentType(MediaType.APPLICATION_JSON)
				 .body(BodyInserters.fromValue(payload))
				 .retrieve()
				 .bodyToMono(Payload.class)
				 .block();

			var assertions = new SoftAssertions();
			assertions.assertThat(result).isNotNull();
			assertions.assertThat(payload.id).isEqualTo(result.id);
			assertions.assertThat(payload.title).isEqualTo(result.title);
			assertions.assertThat(payload.completed).isEqualTo(result.completed);
			assertions.assertAll();
		}

		@DisplayName("DELETE 요청에 선택적으로 응답 클래스를 추가해 응답을 받을 수 있으며 기본적으로는 void 반환한다")
		@Test
		void delete() {
			var id = payload.id;

			var result = webClient.delete()
				 .uri(FAKE_REST_API_URI + id)
				 .exchangeToMono(clientResponse -> Mono.just(clientResponse.statusCode()))
				 .block();

			assertThat(result).isEqualTo(HttpStatus.OK);
		}
	}
}
