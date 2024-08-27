package com.gmoon.springdataredis.helper;

import java.util.Collection;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.gmoon.javacore.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheHelper {

	private final CacheManager cacheManager;

	public void evictAll() {
		Collection<String> cacheNames = cacheManager.getCacheNames();

		if (CollectionUtils.isNotEmpty(cacheNames)) {
			for (String cacheName : cacheNames) {
				evict(cacheName);
			}
		}
	}

	public void evict(String cacheName) {
		evict(cacheName, null);
	}

	public void evict(String cacheName, String cacheKey) {
		Cache cache = cacheManager.getCache(cacheName);
		if (cache == null) {
			return;
		}

		try {
			log.debug("[START] cache {} evict, key: {}", cacheName, cacheKey);
			boolean keyAll = cacheKey == null;
			if (keyAll) {
				cache.clear();
			} else {
				cache.evict(cacheKey);
			}
			log.debug("[END] cache {} evict, key: {}", cacheName, cacheKey);
		} catch (Exception e) {
			// ignore.
			log.warn("cache error.", e);
		}
	}

	public void put(String cacheName, String cacheKey, Object value) {
		org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
		if (cache == null) {
			return;
		}

		try {
			cache.put(cacheKey, value);
		} catch (Exception e) {
			log.warn("cache error: ", e);
		}
	}

	public <T> T get(String cacheName, String cacheKey) {
		Cache cache = cacheManager.getCache(cacheName);
		if (cache == null) {
			return null;
		}

		try {
			Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
			Object value = valueWrapper == null ? null : valueWrapper.get();
			return (T) value;
		} catch (Exception e) {
			log.warn("cache error: ", e);
			return null;
		}
	}
}
