package com.gmoon.cacheinvalidation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.util.Assert;

/**
 * 커밋된 변경 하나와, 그 사실을 알려온 경로를 함께 나른다.
 *
 * <pre>
 * EntityChange.committed(entity, Type.UPDATE, previousState)   // 영속성 컨텍스트를 거친 변경
 * EntityChange.published(entity, Type.UPDATE)                  // JPA 를 거치지 않은 변경
 * </pre>
 */
public record EntityChange(Object entity, Type type, Source source, State previousState) {

	public enum Type {
		INSERT,
		UPDATE,
		DELETE
	}

	/**
	 * 이 변경을 알려온 경로. 무효화가 어느 쪽에서 왔는지 운영에서 가려내는 데 쓴다.
	 */
	public enum Source {

		/** 영속성 컨텍스트를 거쳐 하이버네이트가 자동으로 감지했다. */
		JPA_ENTITY,

		/** 애플리케이션이 직접 발행했다. JPA 를 우회한 쓰기가 여기에 해당한다. */
		PUBLISHED
	}

	public EntityChange {
		Assert.notNull(entity, "entity must not be null");
		Assert.notNull(type, "change type must not be null");
		Assert.notNull(source, "change source must not be null");
		previousState = previousState == null ? State.EMPTY : previousState;
	}

	public static EntityChange committed(Object entity, Type type, State previousState) {
		return new EntityChange(entity, type, Source.JPA_ENTITY, previousState);
	}

	public static EntityChange published(Object entity, Type type) {
		return published(entity, type, State.EMPTY);
	}

	/**
	 * 이전 상태를 아는 발행자가 옛 키 재구성까지 맡길 때 쓴다.
	 * <p>
	 * 영속성 컨텍스트를 거친 변경은 하이버네이트가 이전 상태를 채워 주지만,
	 * 직접 발행하는 쪽은 아무도 채워 주지 않는다. 자연키가 바뀐 변경을 발행한다면
	 * 발행자가 옛 값을 실어야 옛 키가 남지 않는다.
	 */
	public static EntityChange published(Object entity, Type type, State previousState) {
		return new EntityChange(entity, type, Source.PUBLISHED, previousState);
	}

	public Optional<Object> previousValueOf(String propertyName) {
		return previousState.valueOf(propertyName);
	}

	public boolean isTypeOf(Class<?> entityType) {
		return entityType.isInstance(entity);
	}

	/**
	 * 변경 이전의 속성값. 자연키가 바뀐 수정에서 옛 캐시 키를 재구성하는 데 쓴다.
	 * <p>
	 * 하이버네이트가 넘겨주는 배열을 그대로 들면 record 의 동등성이 참조 비교가 되므로
	 * 불변 리스트로 감싸 조회 책임까지 가진다.
	 */
	public record State(List<String> propertyNames, List<Object> values) {

		public static final State EMPTY = new State(List.of(), List.of());

		public State {
			propertyNames = unmodifiableCopyOf(propertyNames);
			values = unmodifiableCopyOf(values);
		}

		public static State of(String[] propertyNames, Object[] values) {
			if (propertyNames == null || values == null) {
				return EMPTY;
			}
			return new State(Arrays.asList(propertyNames), Arrays.asList(values));
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
}
