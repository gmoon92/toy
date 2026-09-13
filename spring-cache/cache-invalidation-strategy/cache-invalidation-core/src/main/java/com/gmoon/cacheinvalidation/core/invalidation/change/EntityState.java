package com.gmoon.cacheinvalidation.core.invalidation.change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record EntityState(List<String> propertyNames, List<Object> values) {

	public static final EntityState EMPTY = new EntityState(List.of(), List.of());

	public EntityState {
		propertyNames = unmodifiableCopyOf(propertyNames);
		values = unmodifiableCopyOf(values);
	}

	public static EntityState of(String[] propertyNames, Object[] values) {
		if (propertyNames == null || values == null) {
			return EMPTY;
		}
		return new EntityState(Arrays.asList(propertyNames), Arrays.asList(values));
	}

	public Optional<Object> valueOf(String propertyName) {
		int index = propertyNames.indexOf(propertyName);
		boolean readable = index >= 0 && index < values.size();
		return readable ? Optional.ofNullable(values.get(index)) : Optional.empty();
	}

	private static <T> List<T> unmodifiableCopyOf(List<T> source) {
		return source == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(source));
	}
}
