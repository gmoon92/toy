package com.gmoon.springaop.business;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Level {
	BASIC(1),
	SILVER(2),
	GOLD(3);

	private final int value;

	public static Level valueOf(int value) {
		return Arrays.stream(Level.values())
			 .filter(level -> level.equalsValueOf(value))
			 .findFirst()
			 .orElseThrow(() -> new AssertionError(String.format("Unknown value: %d", value)));
	}

	private boolean equalsValueOf(int value) {
		return this.value == value;
	}
}
