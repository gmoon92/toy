package com.gmoon.hibernatesecondlevelcache.utils;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CacheUtil {

	private final CacheManager cacheManager;

	public Cache getCache(String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		return cache;
	}

	public Object getValue(String cacheName, String cacheKey) {
		Cache cache = getCache(cacheName);
		Cache.ValueWrapper value = cache.get(cacheKey);
		if (value == null) {
			return null;
		}
		return value.get();
	}

	public void put(String cacheName, String cacheKey, Object cacheValue) {
		Cache cache = getCache(cacheName);
		cache.put(cacheKey, cacheValue);
	}

	public void evict(String cacheName, Object cacheKey) {
		Cache cache = getCache(cacheName);

		cache.evict(cacheKey);
	}

	public void evictAll(String cacheName) {
		Cache cache = getCache(cacheName);

		cache.clear();
	}
}
