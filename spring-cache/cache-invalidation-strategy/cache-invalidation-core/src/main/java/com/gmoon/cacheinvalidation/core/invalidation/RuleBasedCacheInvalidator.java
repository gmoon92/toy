package com.gmoon.cacheinvalidation.core.invalidation;

import com.gmoon.cacheinvalidation.core.invalidation.change.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.change.EntityChange;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RuleBasedCacheInvalidator implements CacheInvalidator {

	private final InvalidationRuleSet rules;
	private final CacheEvictor cacheEvictor;
	private final InvalidationRecorder recorder;

	@Override
	public void invalidate(EntityChange change, ChangeSource source) {
		for (CacheKey entry : rules.resolve(change)) {
			EvictionOutcome outcome = cacheEvictor.evict(entry);
			recorder.recordEviction(entry.cacheName(), source, outcome);
		}
	}
}
