package com.gmoon.cacheinvalidation.core.event;

import java.util.Optional;

import org.springframework.util.Assert;

public record EntityChange(Object entity, ChangeType type, Object id, EntityState previousState) {

	public EntityChange {
		Assert.notNull(entity, "entity must not be null");
		Assert.notNull(type, "change type must not be null");
		previousState = previousState == null ? EntityState.EMPTY : previousState;
	}

	public static EntityChange inserted(Object entity, Object id) {
		return new EntityChange(entity, ChangeType.INSERT, id, EntityState.EMPTY);
	}

	public static EntityChange updated(Object entity, Object id, EntityState previousState) {
		return new EntityChange(entity, ChangeType.UPDATE, id, previousState);
	}

	public static EntityChange updated(Object entity, Object id, Object[] previousValues, String[] propertyNames) {
		return updated(entity, id, EntityState.of(propertyNames, previousValues));
	}

	public static EntityChange deleted(Object entity, Object id) {
		return new EntityChange(entity, ChangeType.DELETE, id, EntityState.EMPTY);
	}

	public Optional<Object> previousValueOf(String propertyName) {
		return previousState.valueOf(propertyName);
	}

	public boolean isTypeOf(Class<?> entityType) {
		return entityType.isInstance(entity);
	}
}
