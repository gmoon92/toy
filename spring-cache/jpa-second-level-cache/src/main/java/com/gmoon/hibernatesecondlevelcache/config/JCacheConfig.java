package com.gmoon.hibernatesecondlevelcache.config;

import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.hibernate.cache.jcache.ConfigSettings;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.jcache.JCacheManager;
import org.redisson.jcache.JCachingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JCacheConfig {

	@Nullable
	private final RedissonClient redissonClient;

	@Bean
	public HibernatePropertiesCustomizer hibernateSecondLevelCacheCustomizer(CacheManager jCacheManager) {
		return (properties) -> properties.put(ConfigSettings.CACHE_MANAGER, jCacheManager);
	}

	@Bean
	public CachingProvider cacheProvider(@Value("${l2.cache-storage}") String cacheStorage) {
		if (CacheType.SIMPLE.name().equals(cacheStorage)) {
			return new EhcacheCachingProvider();
		} else if (CacheType.HAZELCAST.name().equals(cacheStorage)) {
			// Hazelcast server/client
			// return new com.hazelcast.cache.HazelcastCachingProvider();
			// return new com.hazelcast.client.cache.HazelcastClientCachingProvider();

			// Hazelcast Embedded Server
			return new HazelcastServerCachingProvider();
		} else if (CacheType.REDIS.name().equals(cacheStorage)) {
			return new JCachingProvider();
		}

		// String fqcn = "com.hazelcast.cache.HazelcastCachingProvider";
		// CachingProvider cachingProvider = Caching.getCachingProvider(fqcn);
		throw new IllegalArgumentException("cache storage not defined.");
	}

	@Bean
	public CacheManager jCacheManager(CachingProvider cacheProvider) {
		CacheManager jCacheManager = createJCacheManager(cacheProvider);

		// add cache
		createJCaches(jCacheManager);
		return jCacheManager;
	}

	public CacheManager createJCacheManager(CachingProvider cachingProvider) {
		if (cachingProvider instanceof JCachingProvider) { // redisson
			return new JCacheManager(
				 (Redisson)redissonClient,
				 getClass().getClassLoader(),
				 cachingProvider,
				 cachingProvider.getDefaultProperties(),
				 cachingProvider.getDefaultURI()
			);
		}

		return cachingProvider.getCacheManager();
	}

	private void createJCaches(CacheManager jCacheManager) {
		for (CachePolicy policy : CachePolicy.values()) {
			String cacheName = policy.getCacheName();

			// ehcache3 는 @Cache 어노테이션이 선언된 엔티티는 자동으로 캐시 등록
			boolean existsCacheConfig = jCacheManager.getCache(cacheName) != null;
			if (existsCacheConfig) {
				continue;
			}

			jCacheManager.createCache(cacheName, getJCacheConfig(policy.getTtl()));
		}
	}

	private MutableConfiguration<Long, Object> getJCacheConfig(Duration ttl) {
		MutableConfiguration<Long, Object> config = new MutableConfiguration<Long, Object>()
			 //            .setTypes(Long.class, Member.class)
			 .setStoreByValue(false)
			 .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(ttl));

		// add listener config
		config.addCacheEntryListenerConfiguration(createCacheEventLoggerListenerConfig());
		return config;

	}

	private CacheEntryListenerConfiguration<Long, Object> createCacheEventLoggerListenerConfig() {
		return new MutableCacheEntryListenerConfiguration<>(
			 FactoryBuilder.factoryOf(CacheEventLoggerListener.class),
			 null,
			 true,
			 false
		);
	}
}
