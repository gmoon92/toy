package com.gmoon.cacheinvalidation.core.invalidation.change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record PreviousState(List<String> propertyNames, List<Object> values) {

	public static final PreviousState EMPTY = new PreviousState(List.of(), List.of());

	public PreviousState {
		propertyNames = unmodifiableCopyOf(propertyNames);
		values = unmodifiableCopyOf(values);
	}

	public static PreviousState of(String[] propertyNames, Object[] values) {
		if (propertyNames == null || values == null) {
			return EMPTY;
		}
		return new PreviousState(Arrays.asList(propertyNames), Arrays.asList(values));
	}

	public Optional<Object> valueOf(String propertyName) {
		int index = propertyNames.indexOf(propertyName);
		boolean readable = index >= 0 && index < values.size();
		return readable ? Optional.ofNullable(values.get(index)) : Optional.empty();
	}

	public boolean isEmpty() {
		return propertyNames.isEmpty();
	}

	private static <T> List<T> unmodifiableCopyOf(List<T> source) {
		return source == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(source));
	}
}
