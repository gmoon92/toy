package com.gmoon.springsecuritycsrfaspect.csrf.token;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public final class HttpSessionCsrfToken extends BaseCsrfToken {

	@EqualsAndHashCode.Include
	private final String value;

	public static HttpSessionCsrfToken generate() {
		String value = UUID.randomUUID().toString();
		return new HttpSessionCsrfToken(value);
	}
}
