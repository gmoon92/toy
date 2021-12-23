package com.gmoon.springsecuritycsrfaspect.csrf.token;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = "value")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MissingCsrfToken extends BaseCsrfToken {
	public static final MissingCsrfToken INSTANCE;
	private static final String BLANK_TOKEN_VALUE = "_blank";

	private final String value;

	static {
		INSTANCE = new MissingCsrfToken(BLANK_TOKEN_VALUE);
	}
}
