package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Collection;
import java.util.List;

import com.gmoon.cacheinvalidation.core.cache.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.CacheEvictable;

public class CacheEvictableRule implements CacheInvalidationRule {

	@Override
	public boolean supports(EntityChange change) {
		return change.isTypeOf(CacheEvictable.class);
	}

	@Override
	public Collection<CacheEntryRef> resolve(EntityChange change) {
		return change.entity() instanceof CacheEvictable target
			 ? target.cacheEntriesToEvict()
			 : List.of();
	}
}
