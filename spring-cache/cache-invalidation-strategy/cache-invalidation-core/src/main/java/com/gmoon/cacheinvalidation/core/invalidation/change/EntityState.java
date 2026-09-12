package com.gmoon.cacheinvalidation.core.invalidation.change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 한 시점의 엔티티 프로퍼티 값 묶음이다.
 * <p>
 * 어느 시점인지는 이 타입이 아니라 담는 쪽이 정한다.
 * {@code EntityChange.previousState} 는 변경 직전 값을 뜻한다.
 */
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

	public boolean isEmpty() {
		return propertyNames.isEmpty();
	}

	private static <T> List<T> unmodifiableCopyOf(List<T> source) {
		return source == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(source));
	}
}
