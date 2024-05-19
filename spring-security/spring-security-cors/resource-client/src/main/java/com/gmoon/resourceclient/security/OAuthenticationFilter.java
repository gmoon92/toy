package com.gmoon.resourceclient.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.gmoon.resourceclient.util.CookieUtils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class OAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private static final String ORIGIN_OF_RESOURCE_SERVER = "http://localhost:9000";
	private static final String COOKIE_NAME_OF_AUTHORIZATION = HttpHeaders.AUTHORIZATION;

	public OAuthenticationFilter(AuthenticationManager authenticationManager,
		 AuthenticationFailureHandler failureHandler) {
		super(authenticationManager);
		setAuthenticationFailureHandler(failureHandler);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		 AuthenticationException {
		if (!HttpMethod.POST.matches(request.getMethod())) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		String username = obtainUsername(request);
		String password = obtainPassword(request);

		try {
			OAuthenticationToken authRequest = getAuthToken(username, password);
			CookieUtils.addHeaderCookie(response, COOKIE_NAME_OF_AUTHORIZATION, authRequest.getToken());
			return getAuthenticationManager().authenticate(authRequest);
		} catch (WebClientResponseException e) {
			CookieUtils.delete(request, COOKIE_NAME_OF_AUTHORIZATION, response);
			throw new OAuthLoginException(e);
		}
	}

	@Override
	protected String obtainUsername(HttpServletRequest request) {
		return StringUtils.trimToEmpty(request.getParameter(getUsernameParameter()));
	}

	@Override
	protected String obtainPassword(HttpServletRequest request) {
		return StringUtils.trimToEmpty(request.getParameter(getPasswordParameter()));
	}

	// TODO:
	//  https://godekdls.github.io/Reactive%20Spring/webclient/
	//  WebClient ThreadSafe: https://stackoverflow.com/questions/49095366/right-way-to-use-spring-webclient-in-multi-thread-environment
	public OAuthenticationToken getAuthToken(String username, String password) {
		WebClient.RequestBodySpec bodySpec = postLogin(username, password);

		String token = bodySpec.exchangeToFlux(response -> {
			HttpStatus httpStatus = response.statusCode();
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

		return new OAuthenticationToken(token);
	}

	private WebClient.RequestBodySpec postLogin(String username, String password) {
		return defaultWebClient()
			 .method(HttpMethod.POST)
			 .uri(uriBuilder -> uriBuilder
				  .path("login")
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
			 .baseUrl(ORIGIN_OF_RESOURCE_SERVER)
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
