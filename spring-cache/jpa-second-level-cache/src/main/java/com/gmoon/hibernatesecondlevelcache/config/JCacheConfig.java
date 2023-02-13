package com.gmoon.hibernatesecondlevelcache.config;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(L2CacheProperties.class)
public class JCacheConfig {

	@Bean
	public HibernatePropertiesCustomizer hibernateSecondLevelCacheCustomizer(CacheManager jCacheManager) {
		return (properties) -> properties.put(ConfigSettings.CACHE_MANAGER, jCacheManager);
	}

	@Bean
	public CacheManager jCacheManager(CachingProvider jCacheProvider) {
		CacheManager jCacheManager = jCacheProvider.getCacheManager();

		// add cache
		createJCaches(jCacheManager);
		return jCacheManager;
	}

	@Bean
	public CachingProvider jCacheProvider(L2CacheProperties properties) {
		log.info("L2CacheProperties: {}", properties);
		// EH Cache 3
		// return new EhcacheCachingProvider();
		String fqcn = properties.getCachingProviderFqcn();
		return Caching.getCachingProvider(fqcn);
	}

	private void createJCaches(CacheManager jCacheManager) {
		for (CachePolicy cachePolicy : CachePolicy.values()) {
			String cacheName = cachePolicy.getCacheName();

			// ehcache3 는 @Cache 어노테이션이 선언된 엔티티는 자동으로 캐시 등록
			boolean existsAlreadyCacheConfig = jCacheManager.getCache(cacheName) != null;
			if (existsAlreadyCacheConfig) {
				continue;
			}

			Duration ttl = cachePolicy.getTtl();
			jCacheManager.createCache(cacheName, getJCacheConfig(ttl));
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
