package com.gmoon.cacheinvalidation.core.invalidation;

public interface CacheInvalidator {

	void invalidate(EntityChange change, ChangeSource source);
}
