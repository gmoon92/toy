package com.gmoon.hibernatesecondlevelcache.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class SpringCacheConfig {

	@Bean
	public CacheManager cacheManager(javax.cache.CacheManager jCacheManager) {
		return new JCacheCacheManager(jCacheManager);
	}
}
