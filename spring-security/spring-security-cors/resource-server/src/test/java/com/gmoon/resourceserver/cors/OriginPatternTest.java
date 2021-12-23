package com.gmoon.resourceserver.cors;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.cors.CorsConfiguration;

class OriginPatternTest {
	private static final Pattern PORTS_PATTERN = Pattern.compile("(.*):\\[(\\*|\\d+(,\\d+)*)]");

	private List<Pattern> originCheckedPatterns;

	@BeforeEach
	void setUp() {
		// given
		String host = "localhost";
		String patternOfWithoutPort = String.format("**%s", host);
		String patternOfIncludePort = String.format("**%s:**", host);
		originCheckedPatterns = Arrays.asList(
			initPattern(patternOfWithoutPort),
			initPattern(patternOfIncludePort)
		);
	}

	private Pattern initPattern(String patternValue) {
		String portList = null;
		Matcher matcher = PORTS_PATTERN.matcher(patternValue);
		if (matcher.matches()) {
			patternValue = matcher.group(1);
			portList = matcher.group(2);
		}

		patternValue = "\\Q" + patternValue + "\\E";
		patternValue = patternValue.replace("*", "\\E.*\\Q");

		if (portList != null) {
			patternValue += (portList.equals(CorsConfiguration.ALL) ? "(:\\d+)?" :
				":(" + portList.replace(',', '|') + ")");
		}

		return Pattern.compile(patternValue);
	}

	@ParameterizedTest
	@MethodSource("getClientOrigins")
	@DisplayName("참고 CorsConfiguration#checkOrigin")
	void testCheckOrigin_useAllowedOriginPattern(String originToCheck) {
		// when
		boolean isAllowedOrigin = matchPatternFrom(originToCheck);

		// then
		assertThat(isAllowedOrigin).isTrue();
	}

	@ParameterizedTest
	@MethodSource("getInvalidClientOrigins")
	@DisplayName("CorsConfiguration#checkOrigin allowedOriginPatterns 대소문자 체크 불가")
	void testCheckOrigin_invalid_origin(String originToCheck) {
		// when
		boolean isAllowedOrigin = matchPatternFrom(originToCheck);

		// then
		assertThat(isAllowedOrigin).isFalse();
	}

	private boolean matchPatternFrom(String originToCheck) {
		return originCheckedPatterns.stream()
			.anyMatch(pattern -> pattern.matcher(trimTrailingSlash(originToCheck)).matches());
	}

	private String trimTrailingSlash(String origin) {
		return (origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin);
	}

	static Stream<Arguments> getClientOrigins() {
		return Stream.of(
			Arguments.of("http://localhost"),
			Arguments.of("http://localhost:80"),
			Arguments.of("http://localhost:8080"),
			Arguments.of("http://localhost:80/bookmark"),
			Arguments.of("http://localhost:8080/bookmark"),

			Arguments.of("https://localhost"),
			Arguments.of("https://localhost:433"),
			Arguments.of("https://localhost:8443"),
			Arguments.of("https://localhost:433/bookmark"),
			Arguments.of("https://localhost:8443/bookmark")
		);
	}

	static Stream<Arguments> getInvalidClientOrigins() {
		return Stream.of(
			Arguments.of("https://LOCALHOST"),
			Arguments.of("http://locaIhost"),
			Arguments.of("http://locaIhost:80"),
			Arguments.of("http://locaIhost:8080"),
			Arguments.of("http://locaIhost:80/bookmark"),
			Arguments.of("http://locaIhost:8080/bookmark"),

			Arguments.of("https://locaIhost"),
			Arguments.of("https://locaIhost:433"),
			Arguments.of("https://locaIhost:8443"),
			Arguments.of("https://locaIhost:433/bookmark"),
			Arguments.of("https://locaIhost:8443/bookmark")
		);
	}
}
