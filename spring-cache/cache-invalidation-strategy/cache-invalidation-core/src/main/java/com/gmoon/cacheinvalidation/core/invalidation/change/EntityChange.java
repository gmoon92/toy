package com.gmoon.cacheinvalidation.core.invalidation.change;

import java.util.Optional;

import org.springframework.util.Assert;

public record EntityChange(Object entity, Type type, EntityState previousState) {

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

	public static EntityChange inserted(Object entity) {
		return new EntityChange(entity, Type.INSERT, EntityState.EMPTY);
	}

	public static EntityChange updated(Object entity, EntityState previousState) {
		return new EntityChange(entity, Type.UPDATE, previousState);
	}

	public static EntityChange deleted(Object entity) {
		return new EntityChange(entity, Type.DELETE, EntityState.EMPTY);
	}

	public Optional<Object> previousValueOf(String propertyName) {
		return previousState.valueOf(propertyName);
	}

	public boolean isTypeOf(Class<?> entityType) {
		return entityType.isInstance(entity);
	}
}
