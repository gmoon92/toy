package com.gmoon.cacheinvalidation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.gmoon.cacheinvalidation.EntityChange.Source;
import com.gmoon.cacheinvalidation.InvalidationRecorder.Outcome;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DefaultCacheInvalidator implements CacheInvalidator {

	private final List<InvalidationRule> rules;
	private final CacheManager cacheManager;
	private final InvalidationRecorder recorder;

	@Override
	public void invalidate(EntityChange change) {
		PendingInvalidations.collect(resolve(change), this::evictAll);
	}

	private Map<CacheKey, Source> resolve(EntityChange change) {
		Map<CacheKey, Source> entries = new LinkedHashMap<>();
		for (InvalidationRule rule : rules) {
			resolveInIsolation(rule, change).forEach(entry -> entries.putIfAbsent(entry, change.source()));
		}
		return entries;
	}

	private Collection<CacheKey> resolveInIsolation(InvalidationRule rule, EntityChange change) {
		try {
			return rule.supports(change) ? rule.resolve(change) : List.of();
		} catch (RuntimeException e) {
			recorder.recordRuleFailure(rule.getClass().getSimpleName());
			log.warn("cache invalidation rule failed. rule: {}", rule.getClass().getSimpleName(), e);
			return List.of();
		}
	}

	private void evictAll(Map<CacheKey, Source> entries) {
		entries.forEach((entry, source) ->
			 recorder.recordEviction(entry.cacheName(), source, evict(entry)));
	}

	private Outcome evict(CacheKey entry) {
		try {
			Cache cache = cacheManager.getCache(entry.cacheName());
			if (cache == null) {
				log.warn("cache not registered. name: {}", entry.cacheName());
				return Outcome.CACHE_NOT_REGISTERED;
			}
			cache.evict(entry.key());
			return Outcome.EVICT_REQUESTED;
		} catch (RuntimeException e) {
			log.warn("cache eviction failed. entry: {}", entry, e);
			return Outcome.FAILED;
		}
	}
}
