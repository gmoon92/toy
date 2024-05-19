package com.gmoon.springasync.server;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum Protocol {
	HTTPS("https", true),
	HTTP("http");

	private static final Map<Boolean, Protocol> OPERATOR_MAP = Collections.unmodifiableMap(Stream.of(values())
		 .collect(Collectors.toMap(Protocol::isUseSsl, Function.identity())));

	private final String value;
	private final boolean useSsl;

	Protocol(String value) {
		this(value, false);
	}

	Protocol(String value, boolean useSsl) {
		this.value = value;
		this.useSsl = useSsl;
	}

	public static Protocol of(boolean useSsl) {
		return OPERATOR_MAP.get(useSsl);
	}
}
