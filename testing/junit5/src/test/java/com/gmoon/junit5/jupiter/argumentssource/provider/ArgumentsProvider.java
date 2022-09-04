package com.gmoon.junit5.jupiter.argumentssource.provider;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgumentsProvider {

	static Stream<String> stringProvider() {
		return Stream.of("gmoon", "guest");
	}

	static IntStream negativeNumberProvider() {
		return IntStream.range(-10, 0).skip(0);
	}
}
