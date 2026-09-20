package com.gmoon.cacheinvalidation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class RuleBasedCacheInvalidator implements CacheInvalidator {

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
