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

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gmoon.hibernatesecondlevelcache.member.Member;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableCaching
public class JCacheConfig extends CacheConfig {

	@Bean
	public CacheManager jCacheManager() {
		CachingProvider provider = getCachingProvider();
		CacheManager jCacheManager = provider.getCacheManager();

		// add cache
		createJCaches(jCacheManager);
		return jCacheManager;
	}

	private CachingProvider getCachingProvider() {
		// return new EhcacheCachingProvider();
		String fqcn = "org.ehcache.jsr107.EhcacheCachingProvider";
		return Caching.getCachingProvider(fqcn);
	}

	private void createJCaches(CacheManager jCacheManager) {
		for (JCachePolicy cachePolicy : JCachePolicy.values()) {
			String cacheName = cachePolicy.cacheName;
			Duration ttl = cachePolicy.ttl;
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

	@RequiredArgsConstructor
	enum JCachePolicy {

		MEMBER(Member.class);

		private final String cacheName;

		private final Duration ttl;
		JCachePolicy(Class<?> entity) {
			this(entity.getName(), Duration.ONE_MINUTE);
		}
	}
}
