package com.gmoon.hibernatesecondlevelcache.config;

import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;
import lombok.ToString;

@ConstructorBinding
@ConfigurationProperties(prefix = "l2")
@ToString
@Getter
public class L2CacheProperties {

	private final String cacheStorage;
	private final String cachingProviderFqcn;

	public L2CacheProperties(String cacheStorage) {
		this.cacheStorage = cacheStorage;
		this.cachingProviderFqcn = getCachingProviderFqcn(cacheStorage);
	}

	private String getCachingProviderFqcn(String cacheStorage) {
		if (CacheType.EHCACHE.name().equals(cacheStorage)) {
			return "org.ehcache.jsr107.EhcacheCachingProvider";
		} else if (CacheType.HAZELCAST.name().equals(cacheStorage)){
			return "com.hazelcast.cache.HazelcastCachingProvider";
		} else if (CacheType.REDIS.name().equals(cacheStorage)) {
			return "org.redisson.jcache.JCachingProvider";
		}

		throw new IllegalArgumentException("cache storage not defined.");
	}
}
