package com.gmoon.springreactive.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

/**
 * https://godekdls.github.io/Reactive%20Spring/webclient/
 *
 * WebClient Thread Safe: https://stackoverflow.com/questions/49095366/right-way-to-use-spring-webclient-in-multi-thread-environment
 * */
@Configuration
public class WebClientConfig {
	private static final int MAX_MILLISECOND_OF_RESPONSE_TIMEOUT = 5_000;

	@Bean
	public WebClient webClient() {
		HttpClient httpClient = connectionTimeoutClient();
		return WebClient.builder()
			 .baseUrl("http://localhost:9000")
			 .clientConnector(new ReactorClientHttpConnector(httpClient))
			 .build();
	}

	private HttpClient connectionTimeoutClient() {
		return HttpClient.create()
			 // response connect timeout config
			 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, MAX_MILLISECOND_OF_RESPONSE_TIMEOUT)
			 .responseTimeout(Duration.ofMillis(MAX_MILLISECOND_OF_RESPONSE_TIMEOUT))
			 .doOnConnected(conn ->
				  // read / write response timeout config
				  conn.addHandlerLast(
							new ReadTimeoutHandler(MAX_MILLISECOND_OF_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS))
					   .addHandlerLast(
							new WriteTimeoutHandler(MAX_MILLISECOND_OF_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS))
			 );
	}
}

