package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Set;

import com.gmoon.cacheinvalidation.core.cache.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.CacheEvictor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntityChangeInvalidator {

	private final CacheInvalidationRules rules;
	private final CacheEvictor cacheEvictor;

	public void invalidate(EntityChange change) {
		Set<CacheEntryRef> entries = rules.resolve(change);
		for (CacheEntryRef entry : entries) {
			cacheEvictor.evict(entry);
		}
	}
}
