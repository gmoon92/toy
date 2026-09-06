package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Collection;

import com.gmoon.cacheinvalidation.core.cache.CacheEntryRef;

public interface CacheInvalidationRule {

	boolean supports(EntityChange change);

	Collection<CacheEntryRef> resolve(EntityChange change);
}
