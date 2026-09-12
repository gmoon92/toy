package com.gmoon.cacheinvalidation.core.invalidation;

import com.gmoon.cacheinvalidation.core.cache.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.CacheEvictor;
import com.gmoon.cacheinvalidation.core.cache.EvictionOutcome;
import com.gmoon.cacheinvalidation.core.resilience.InvalidationRecorder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntityChangeInvalidator implements CommitSignalSink {

	private final CacheInvalidationRules rules;
	private final CacheEvictor cacheEvictor;
	private final InvalidationRecorder recorder;

	@Override
	public void accept(EntityChange change, InvalidationSource source) {
		for (CacheEntryRef entry : rules.resolve(change)) {
			EvictionOutcome outcome = cacheEvictor.evict(entry);
			recorder.recordEviction(entry.cacheName(), source, outcome);
		}
	}
}
