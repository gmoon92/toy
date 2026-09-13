package com.gmoon.cacheinvalidation.core.invalidation.event;

import java.util.Optional;

import org.springframework.util.Assert;

public record EntityChange(Object entity, Type type, Object id, EntityState previousState) {

	public enum Type {
		INSERT,
		UPDATE,
		DELETE
	}

	public EntityChange {
		Assert.notNull(entity, "entity must not be null");
		Assert.notNull(type, "change type must not be null");
		previousState = previousState == null ? EntityState.EMPTY : previousState;
	}

	public static EntityChange inserted(Object entity, Object id) {
		return new EntityChange(entity, Type.INSERT, id, EntityState.EMPTY);
	}

	public static EntityChange updated(Object entity, Object id, EntityState previousState) {
		return new EntityChange(entity, Type.UPDATE, id, previousState);
	}

	public static EntityChange updated(Object entity, Object id, Object[] previousValues, String[] propertyNames) {
		return updated(entity, id, EntityState.of(propertyNames, previousValues));
	}

	public static EntityChange deleted(Object entity, Object id) {
		return new EntityChange(entity, Type.DELETE, id, EntityState.EMPTY);
	}

	public Optional<Object> previousValueOf(String propertyName) {
		return previousState.valueOf(propertyName);
	}

	public boolean isTypeOf(Class<?> entityType) {
		return entityType.isInstance(entity);
	}
}
