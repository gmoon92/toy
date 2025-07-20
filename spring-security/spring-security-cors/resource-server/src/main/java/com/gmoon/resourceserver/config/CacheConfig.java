package com.gmoon.resourceserver.config;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gmoon.resourceserver.constants.CacheName;

@Configuration
@EnableCaching
public class CacheConfig {
	@Bean
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(getCaches());
		return cacheManager;
	}

	private List<Cache> getCaches() {
		return Stream.of(CacheName.values())
			 .map(CacheName::getValue)
			 .map(ConcurrentMapCache::new)
			 .collect(Collectors.toList());
	}
}
