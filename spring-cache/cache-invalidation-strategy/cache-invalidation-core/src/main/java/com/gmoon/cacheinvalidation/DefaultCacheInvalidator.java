package com.gmoon.cacheinvalidation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DefaultCacheInvalidator implements CacheInvalidator {

	private final List<InvalidationRule> rules;
	private final CacheManager cacheManager;

	/**
	 * 신호는 모두 커밋 이후에 도착한다. 여기서 예외가 새어나가면
	 * 이미 끝난 트랜잭션의 호출자가 롤백으로 오해한다.
	 */
	@Override
	public void invalidate(EntityChange change) {
		try {
			PendingInvalidations.collect(resolve(change), this::evictAll);
		} catch (RuntimeException e) {
			log.warn("cache invalidation failed after commit. the transaction stays committed", e);
		}
	}

	private Set<CacheKey> resolve(EntityChange change) {
		Set<CacheKey> entries = new LinkedHashSet<>();
		for (InvalidationRule rule : rules) {
			entries.addAll(
				 resolveInIsolation(
					  rule,
					  change
				 )
			);
		}
		return entries;
	}

	private Collection<CacheKey> resolveInIsolation(InvalidationRule rule, EntityChange change) {
		try {
			return rule.supports(change) ? rule.resolve(change) : List.of();
		} catch (RuntimeException e) {
			log.warn("cache invalidation rule failed. rule: {}", rule.getClass().getSimpleName(), e);
			return List.of();
		}
	}

	private void evictAll(Set<CacheKey> entries) {
		entries.forEach(this::evict);
	}

	private void evict(CacheKey entry) {
		try {
			Cache cache = cacheManager.getCache(entry.cacheName());
			if (cache == null) {
				log.warn("cache not registered. name: {}", entry.cacheName());
				return;
			}
			cache.evict(entry.key());
		} catch (RuntimeException e) {
			log.warn("cache eviction failed. entry: {}", entry, e);
		}
	}
}
