package com.gmoon.cacheinvalidation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

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
		for (CacheKey entry : resolve(change)) {
			recorder.recordEviction(entry.cacheName(), change.source(), evict(entry));
		}
	}

	private Set<CacheKey> resolve(EntityChange change) {
		return rules.stream()
			 .map(rule -> resolveInIsolation(rule, change))
			 .flatMap(Collection::stream)
			 .collect(Collectors.toCollection(LinkedHashSet::new));
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
