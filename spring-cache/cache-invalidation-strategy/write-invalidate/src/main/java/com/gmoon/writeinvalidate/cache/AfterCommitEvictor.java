package com.gmoon.writeinvalidate.cache;

import java.util.Collection;
import java.util.Set;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 안이면 커밋 이후에, 밖이면 바로 지운다.
 */
@RequiredArgsConstructor
@Slf4j
final class AfterCommitEvictor {

	private final CacheManager cacheManager;
	private final InvalidationMetrics metrics;

	void evict(Set<CacheKey> cacheKeys) {
		if (cacheKeys.isEmpty()) {
			return;
		}
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			evictAfterCommit(cacheKeys);
			return;
		}
		evictEach(cacheKeys);
	}

	private void evictAfterCommit(Set<CacheKey> cacheKeys) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status == STATUS_COMMITTED) {
					evictEach(cacheKeys);
				}
			}
		});
	}

	private void evictEach(Collection<CacheKey> cacheKeys) {
		cacheKeys.forEach(this::evictOne);
	}

	private void evictOne(CacheKey cacheKey) {
		try {
			Cache cache = cacheManager.getCache(cacheKey.cacheName());
			if (cache == null) {
				log.warn("cache not registered. name: {}", cacheKey.cacheName());
				metrics.evictionFailed(cacheKey.cacheName());
				return;
			}
			cache.evict(cacheKey.key());
		} catch (RuntimeException e) {
			log.warn("cache eviction failed. key: {}", cacheKey, e);
			metrics.evictionFailed(cacheKey.cacheName());
		}
	}
}
