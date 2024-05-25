package com.gmoon.springreactive.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

class WebClientConfigTest {

	@Test
	void name() {

	}

	public String token(String username, String password) {
		WebClient.RequestBodySpec bodySpec = postLoginSpec(username, password);

		String token = bodySpec.exchangeToFlux(response -> {
			HttpStatusCode httpStatus = response.statusCode();
			if (HttpStatus.OK.equals(httpStatus)) {
				ClientResponse.Headers headers = response.headers();
				List<String> tokens = headers.header(HttpHeaders.AUTHORIZATION);
				return Flux.just(tokens.get(0));
			}

			return response.createException()
				 .flatMap(Mono::error)
				 .map(String::valueOf)
				 .flux();
		}).blockFirst();

		return token;
	}

	private WebClient.RequestBodySpec postLoginSpec(String username, String password) {
		return defaultWebClient()
			 .method(HttpMethod.POST)
			 .uri(uriBuilder -> uriBuilder
				  .path("/login")
				  .queryParam("username", username)
				  .queryParam("password", password)
				  .build())
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON)
			 .acceptCharset(StandardCharsets.UTF_8);
	}

	private WebClient defaultWebClient() {
		HttpClient httpClient = connectionTimeoutClient();
		return WebClient.builder()
			 .baseUrl("http://localhost:9000")
			 .clientConnector(new ReactorClientHttpConnector(httpClient))
			 .build();
	}

	private HttpClient connectionTimeoutClient() {
		final int MAX_OF_RESPONSE_TIMEOUT = 5_000;
		return HttpClient.create()
			 // response connect timeout config
			 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, MAX_OF_RESPONSE_TIMEOUT)
			 .responseTimeout(Duration.ofMillis(MAX_OF_RESPONSE_TIMEOUT))
			 .doOnConnected(conn ->
				  // read / write response timeout config
				  conn.addHandlerLast(new ReadTimeoutHandler(MAX_OF_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS))
					   .addHandlerLast(new WriteTimeoutHandler(MAX_OF_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS))
			 );
	}
}
