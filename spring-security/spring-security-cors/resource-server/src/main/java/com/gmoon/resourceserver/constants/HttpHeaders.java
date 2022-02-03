package com.gmoon.resourceserver.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpHeaders extends org.springframework.http.HttpHeaders {
	public static final String X_FORWARDED_FOR = "X-Forwarded-For";
}
