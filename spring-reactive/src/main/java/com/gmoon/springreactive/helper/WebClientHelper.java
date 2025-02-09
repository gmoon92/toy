package com.gmoon.springreactive.helper;

import java.util.Map;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WebClientHelper {

	private final WebClient webClient;

	public WebClientHelperSpec<?> get() {
		return new WebClientHelperSpec<>(webClient.get());
	}

	public WebClientHelperSpec<WebClient.RequestBodyUriSpec> post() {
		return new WebClientHelperSpec<>(webClient.post());
	}

	public WebClientHelperSpec<WebClient.RequestBodyUriSpec> patch() {
		return new WebClientHelperSpec<>(webClient.patch());
	}

	public WebClientHelperSpec<WebClient.RequestBodyUriSpec> put() {
		return new WebClientHelperSpec<>(webClient.put());
	}

	public WebClientHelperSpec<?> delete() {
		return new WebClientHelperSpec<>(webClient.delete());
	}

	/**
	 * It is a simple combination of WebClient's Spec Classes.
	 *
	 * @see WebClient.UriSpec
	 * @see WebClient.RequestHeadersSpec
	 * @see WebClient.RequestHeadersUriSpec
	 * @see WebClient.RequestBodySpec
	 * @see WebClient.RequestBodyUriSpec
	 * @see WebClient.ResponseSpec
	 */
	public static class WebClientHelperSpec<S extends WebClient.RequestHeadersUriSpec<?>> {

		protected final S requestHeadersUriSpec;

		private WebClientHelperSpec(S requestHeadersUriSpec) {
			this.requestHeadersUriSpec = requestHeadersUriSpec;
		}

		public WebClientHelperSpec<S> uri(String uri) {
			requestHeadersUriSpec.uri(uri);
			return this;
		}

		public WebClientHelperSpec<S> uri(String uri, Map<String, String> queryParams) {
			return uri(uri, toMultiValueMap(queryParams));
		}

		public WebClientHelperSpec<S> uri(String uri, MultiValueMap<String, String> queryParams) {
			requestHeadersUriSpec.uri(uri, uriFunction -> uriFunction
				 .queryParams(queryParams)
				 .build());
			return this;
		}

		public WebClientHelperSpec<S> accept(MediaType accept) {
			requestHeadersUriSpec.accept(accept);
			return this;
		}

		public WebClientHelperSpec<S> headers(Map<String, String> headers) {
			requestHeadersUriSpec.headers(httpHeaders -> httpHeaders.addAll(toMultiValueMap(headers)));
			return this;
		}

		protected <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, V> params) {
			MultiValueMap<K, V> multiValueMap = new LinkedMultiValueMap<>();
			for (Map.Entry<K, V> entry : params.entrySet()) {
				multiValueMap.add(entry.getKey(), entry.getValue());
			}
			return multiValueMap;
		}

		/**
		 * <p>
		 * A payload within a <b>GET or DELETE</b> request message has no defined semantics;
		 * sending a payload body on a GET or DELETE request might cause some existing
		 * implementations to reject the request.
		 * </p>
		 *
		 * <p>WebClient was designed to divide Specs into two kinds.
		 * {@link WebClient.RequestBodySpec} can send a payload body, the others can not send a payload body.</p>
		 *
		 * @see <a href="https://arc.net/l/quote/zpjnmwfz">rfc 7231</a>
		 */
		public <T> WebClientHelperSpec<S> bodyValue(T body) {
			if (requestHeadersUriSpec instanceof WebClient.RequestBodySpec requestBodySpec) {
				requestBodySpec.bodyValue(body);
			} else {
				throw new UnsupportedOperationException("GET and DELETE requests do not support body payloads.");
			}
			return this;
		}

		public <T> Mono<T> bodyToMono(Class<T> returnType) {
			return retrieve().bodyToMono(returnType);
		}

		public <T> Flux<T> bodyToFlux(Class<T> returnType) {
			return retrieve().bodyToFlux(returnType);
		}

		private WebClient.ResponseSpec retrieve() {
			return requestHeadersUriSpec.retrieve()
				 .onRawStatus(this::isCustomError, this::createCustomException)
				 .onStatus(HttpStatusCode::isError, ClientResponse::createException);
		}

		public <T> Mono<T> exchangeToMono(Function<ClientResponse, ? extends Mono<T>> responseHandler) {
			return requestHeadersUriSpec
				 .exchangeToMono(response -> {
						  if (response.statusCode().isError()) {
							  return createCustomException(response)
								   .flatMap(Mono::error);
						  }

						  return responseHandler.apply(response);
					  }
				 );
		}

		private boolean isCustomError(int statusCode) {
			HttpStatus httpStatus = HttpStatus.resolve(statusCode);
			return httpStatus == null || httpStatus.isError();
		}

		private Mono<? extends Throwable> createCustomException(ClientResponse clientResponse) {
			return clientResponse
				 .bodyToMono(String.class)
				 .flatMap(responseBody -> {
					 String message = String.format(
						  "ERROR: HTTP %d %s -> Response: %s",
						  clientResponse.statusCode().value(),
						  clientResponse.request().getURI(),
						  responseBody
					 );
					 return Mono.error(new RuntimeException(message));
				 });
		}
	}
}
