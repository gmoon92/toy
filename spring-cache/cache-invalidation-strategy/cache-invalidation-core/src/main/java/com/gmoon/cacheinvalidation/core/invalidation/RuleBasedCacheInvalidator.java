package com.gmoon.cacheinvalidation.core.invalidation;

import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEvictor;
import com.gmoon.cacheinvalidation.core.cache.eviction.EvictionOutcome;
import com.gmoon.cacheinvalidation.core.metrics.InvalidationRecorder;

import lombok.RequiredArgsConstructor;
import com.gmoon.cacheinvalidation.core.event.ChangeSource;
import com.gmoon.cacheinvalidation.core.event.EntityChange;

@RequiredArgsConstructor
public class RuleBasedCacheInvalidator implements CacheInvalidator {

	private final InvalidationRules rules;
	private final CacheEvictor cacheEvictor;
	private final InvalidationRecorder recorder;

	@Override
	public void invalidate(EntityChange change, ChangeSource source) {
		for (CacheEntryRef entry : rules.resolve(change)) {
			EvictionOutcome outcome = cacheEvictor.evict(entry);
			recorder.recordEviction(entry.cacheName(), source, outcome);
		}
	}
}
