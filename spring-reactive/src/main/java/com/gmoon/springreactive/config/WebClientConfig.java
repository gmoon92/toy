package com.gmoon.springreactive.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.LoggingCodecSupport;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.http.codec.xml.Jaxb2XmlEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.gmoon.javacore.function.UncheckedLambdaExceptionHandler;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

/**
 * @see <a href="https://stackoverflow.com/questions/49095366/right-way-to-use-spring-webclient-in-multi-thread-environment">WebClient Thread Safe</a>
 * @see <a href="https://godekdls.github.io/Reactive%20Spring/webclient/">godekdls.github.io - webclient</a>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

	private final WebClientProperties properties;

	@Primary
	@Bean
	public WebClient webClient() {
		return webClientBuilder().build();
	}

	/**
	 * @apiNote 각 도메인에 대해 커스터마이징된 {@link WebClient}를 사용하려면,
	 * webClientBuilder를 주입받아 별도의 WebClient Bean을 생성할 수 있습니다.
	 * {@link Primary} 애노테이션이 붙은 WebClient Bean은 기본 WebClient로 사용됩니다.
	 * 따라서 커스터마이즈된 WebClient를 사용하려면 {@link Qualifier} 애노테이션을 사용해야 합니다.
	 */
	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder()
			 .clientConnector(
				  new ReactorClientHttpConnector(
					   HttpClient.create(getConnectionProvider()) // 커스텀 커넥션 프로바이더 사용
							// 로그 출력 설정
							.wiretap(
								 "com.gmoon.springreactive.webclient",
								 LogLevel.DEBUG,
								 AdvancedByteBufFormat.TEXTUAL
							)
							// SSL 보안 설정 (신뢰되지 않는 인증서 허용)
							.secure(UncheckedLambdaExceptionHandler.consumer(
								 sslContextSpec -> sslContextSpec.sslContext(
									  SslContextBuilder.forClient()
										   .trustManager(InsecureTrustManagerFactory.INSTANCE)
										   .build()
								 )
							))
							// 연결 시간 제한 설정, response connect timeout config
							.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int)properties.getConnectTimeout().toMillis())
							// 응답 시간 제한
							.responseTimeout(properties.getResponseTimeout())
							// 연결 후 타임아웃 핸들러 설정, read / write response timeout config
							.doOnConnected(this::handleConnectionTimeout)
				  )
			 )
			 // 기본 헤더 설정
			 .defaultHeaders(this::handleDefaultHeaders)
			 // 커스텀 익스체인지 전략 설정
			 .exchangeStrategies(handleExchangeStrategies())
			 // 요청 로깅 필터
			 .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
				 log.debug("Request :{} {}", clientRequest.method(), clientRequest.url());
				 log.debug("Request Headers :{}", clientRequest.headers());
				 return Mono.just(clientRequest);
			 }))
			 // 응답 로깅 필터
			 .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
				 HttpStatusCode httpStatusCode = clientResponse.statusCode();
				 log.debug("Response :{}", httpStatusCode);
				 log.debug("Response Headers :{}", clientResponse.headers().asHttpHeaders());
				 return Mono.just(clientResponse);
			 }));
	}

	private void handleConnectionTimeout(Connection connection) {
		// read / write response timeout config
		connection
			 .addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS))
			 .addHandlerLast(new WriteTimeoutHandler(properties.getWriteTimeout().toMillis(), TimeUnit.MILLISECONDS));
	}

	/**
	 * @apiNote 연결 프로바이더 설정
	 * <pre>
	 * 서버에서 클라이언트로 연결을 종료할 때 RST(Reset) 플래그 없이 종료된 경우,
	 * 클라이언트는 연결이 종료되었음을 알 수 없습니다.
	 * 만약 클라이언트가 종료된 연결로 서버에 요청을 보내면,
	 * 'connection reset by peer'와 같은 오류가 발생할 수 있습니다.
	 *
	 * `maxIdleTime`과 `maxLifeTime`의 제약은 서버의 `keepAliveTimeout` 설정에 따라 다릅니다.
	 * 예를 들어, Tomcat이나 Undertow의 `server.xml`에서 기본적으로 `keepAliveTimeout`은 20초입니다.
	 * 따라서 `maxIdleTime`과 `maxLifeTime`은 20초보다 작은 값으로 설정하는 것이 좋습니다.
	 * </pre>
	 * @see <a href="https://projectreactor.io/docs/netty/release/reference/index.html#connection-pool-timeout">Reactor Netty 문서</a>
	 * @see <a href="https://github.com/reactor/reactor-netty/issues/1092#issuecomment-648651826">Reactor Netty 이슈</a>
	 */
	private ConnectionProvider getConnectionProvider() {
		return ConnectionProvider
			 .builder("provider")
			 .maxIdleTime(properties.getMaxIdleTime()) // 연결이 유휴 상태로 있을 수 있는 최대 시간
			 .maxLifeTime(properties.getMaxLifeTime()) // 연결의 최대 수명
			 .lifo() // LIFO 방식으로 연결을 관리
			 .build();
	}

	/**
	 * @apiNote WebClient의 인코딩/디코딩 전략을 설정
	 */
	private ExchangeStrategies handleExchangeStrategies() {
		// 데이터 코덱 설정
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
			 .codecs(configurer -> {
				 configurer.defaultCodecs().maxInMemorySize((int)properties.getMaxInMemorySize().toBytes()); // 최대 메모리 크기 설정
				 configurer.defaultCodecs().jaxb2Encoder(new Jaxb2XmlEncoder()); // JAXB2 XML 인코더 설정
				 configurer.defaultCodecs().jaxb2Decoder(new Jaxb2XmlDecoder()); // JAXB2 XML 디코더 설정
			 })
			 .build();

		// 요청 디테일 로깅 활성화
		exchangeStrategies
			 .messageWriters().stream()
			 .filter(LoggingCodecSupport.class::isInstance)
			 .forEach(writer -> ((LoggingCodecSupport)writer).setEnableLoggingRequestDetails(true));
		return exchangeStrategies;
	}

	/**
	 * @apiNote 기본 HTTP 헤더 설정
	 */
	private void handleDefaultHeaders(HttpHeaders httpHeaders) {
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.setAccept(List.of(
			 MediaType.APPLICATION_JSON,
			 MediaType.APPLICATION_XML,
			 MediaType.TEXT_XML,
			 MediaType.TEXT_PLAIN
		));
	}
}
