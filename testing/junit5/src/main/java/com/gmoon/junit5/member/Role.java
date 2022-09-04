package com.gmoon.junit5.member;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
	ADMIN(0),
	MANAGER(1),
	USER(2);

	private final int value;

	public static final Map<Integer, Role> ALL = Stream.of(values())
		.collect(collectingAndThen(
			toMap(Role::getValue, Function.identity()),
			Collections::unmodifiableMap
		));
}
