package com.gmoon.springsecurityjwt.jwt;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthenticationSchema {
	BASIC("Basic"),
	BEARER("Bearer"),
	DIGEST("Digest"),
	HOBA("HOBA"),
	MUTUAL("Mutual"),
	NEGOTIATE("Negotiate"),
	OAUTH("OAuth"),
	SHA1("SCRAM-SHA-1"),
	SHA256("SCRAM-SHA-256"),
	VAPID("vapid");

	private static final Map<String, AuthenticationSchema> MAP = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(AuthenticationSchema::getName, Function.identity())));

	private final String name;

	public static void checkValidSchema(String schema) {
		if (!contain(schema)) {
			throw new IllegalArgumentException(String.format("Invalid authentication schema name: %s", schema));
		}
	}

	private static boolean contain(String schema) {
		return MAP.containsKey(schema);
	}
}
