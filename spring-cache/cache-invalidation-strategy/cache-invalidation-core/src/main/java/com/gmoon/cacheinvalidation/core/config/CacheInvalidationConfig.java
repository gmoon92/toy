package com.gmoon.cacheinvalidation.core.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gmoon.cacheinvalidation.core.cache.CacheEvictor;
import com.gmoon.cacheinvalidation.core.cache.CachePolicies;
import com.gmoon.cacheinvalidation.core.cache.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.event.CacheEvictEventListener;
import com.gmoon.cacheinvalidation.core.invalidation.CacheEvictableRule;
import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidationRule;
import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidationRules;
import com.gmoon.cacheinvalidation.core.invalidation.EntityChangeInvalidator;
import com.gmoon.cacheinvalidation.core.metrics.CacheStatisticsReporter;
import com.gmoon.cacheinvalidation.core.metrics.DatabaseQueryCounter;
import com.gmoon.cacheinvalidation.core.resilience.CacheFailureRecorder;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@Import(RedisCacheConfig.class)
@EnableConfigurationProperties(CacheInvalidationProperties.class)
public class CacheInvalidationConfig {

	@Bean
	public CachePolicyRegistry cachePolicyRegistry(List<CachePolicies> sources) {
		return new CachePolicyRegistry(sources);
	}

	@Bean
	public CacheFailureRecorder cacheFailureRecorder() {
		return new CacheFailureRecorder();
	}

	@Bean
	public CacheEvictor cacheEvictor(CacheManager cacheManager, CacheFailureRecorder failureRecorder) {
		return new CacheEvictor(cacheManager, failureRecorder);
	}

	@Bean
	public CacheEvictableRule cacheEvictableRule() {
		return new CacheEvictableRule();
	}

	@Bean
	public CacheInvalidationRules cacheInvalidationRules(List<CacheInvalidationRule> rules) {
		return new CacheInvalidationRules(rules);
	}

	@Bean
	public EntityChangeInvalidator entityChangeInvalidator(CacheInvalidationRules rules, CacheEvictor cacheEvictor) {
		return new EntityChangeInvalidator(rules, cacheEvictor);
	}

	@Bean
	public CacheEvictEventListener cacheEvictEventListener(EntityChangeInvalidator invalidator) {
		return new CacheEvictEventListener(invalidator);
	}

	@Bean
	public CacheEvictEventListenerRegistrar cacheEvictEventListenerRegistrar(
		 EntityManagerFactory entityManagerFactory,
		 CacheEvictEventListener cacheEvictEventListener
	) {
		return new CacheEvictEventListenerRegistrar(entityManagerFactory, cacheEvictEventListener);
	}

	@Bean
	public CacheStatisticsReporter cacheStatisticsReporter(CacheManager cacheManager) {
		return new CacheStatisticsReporter(cacheManager);
	}

	@Bean
	public DatabaseQueryCounter databaseQueryCounter(EntityManagerFactory entityManagerFactory) {
		return new DatabaseQueryCounter(entityManagerFactory);
	}
}
