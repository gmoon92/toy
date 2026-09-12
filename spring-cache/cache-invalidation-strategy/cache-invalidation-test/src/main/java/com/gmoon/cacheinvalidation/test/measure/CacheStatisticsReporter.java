package com.gmoon.cacheinvalidation.test.measure;

import java.util.Collection;
import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.RedisCache;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CacheStatisticsReporter {

	private final CacheManager cacheManager;

	public Optional<CacheStatistics> statisticsOf(String cacheName) {
		return redisCacheOf(cacheName).map(RedisCache::getStatistics);
	}

	public Collection<String> cacheNames() {
		return cacheManager.getCacheNames();
	}

	public void reset() {
		cacheNames().forEach(cacheName -> redisCacheOf(cacheName).ifPresent(RedisCache::clearStatistics));
	}

	private Optional<RedisCache> redisCacheOf(String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		return cache instanceof RedisCache redisCache ? Optional.of(redisCache) : Optional.empty();
	}
}
