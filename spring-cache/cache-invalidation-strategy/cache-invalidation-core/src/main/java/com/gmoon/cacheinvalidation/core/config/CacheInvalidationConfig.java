package com.gmoon.cacheinvalidation.core.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gmoon.cacheinvalidation.core.cache.CacheEvictor;
import com.gmoon.cacheinvalidation.core.cache.CachePolicies;
import com.gmoon.cacheinvalidation.core.cache.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.event.HibernateCommitSignalListener;
import com.gmoon.cacheinvalidation.core.event.SpringCommitSignalListener;
import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidationOwnershipValidator;
import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidationRule;
import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidationRules;
import com.gmoon.cacheinvalidation.core.invalidation.CommitSignalSink;
import com.gmoon.cacheinvalidation.core.invalidation.EntityChangeInvalidator;
import com.gmoon.cacheinvalidation.core.metrics.CacheStatisticsReporter;
import com.gmoon.cacheinvalidation.core.metrics.DatabaseQueryCounter;
import com.gmoon.cacheinvalidation.core.resilience.CacheFailureRecorder;
import com.gmoon.cacheinvalidation.core.resilience.InvalidationRecorder;

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
	public InvalidationRecorder invalidationRecorder() {
		return new InvalidationRecorder();
	}

	@Bean
	public CacheEvictor cacheEvictor(CacheManager cacheManager, CacheFailureRecorder failureRecorder) {
		return new CacheEvictor(cacheManager, failureRecorder);
	}

	@Bean
	public CacheInvalidationRules cacheInvalidationRules(
		 List<CacheInvalidationRule> rules,
		 InvalidationRecorder recorder
	) {
		return new CacheInvalidationRules(rules, recorder);
	}

	@Bean
	public CacheInvalidationOwnershipValidator cacheInvalidationOwnershipValidator(
		 CachePolicyRegistry registry,
		 CacheInvalidationRules rules
	) {
		return new CacheInvalidationOwnershipValidator(registry, rules);
	}

	@Bean
	public CommitSignalSink commitSignalSink(
		 CacheInvalidationRules rules,
		 CacheEvictor cacheEvictor,
		 InvalidationRecorder recorder
	) {
		return new EntityChangeInvalidator(rules, cacheEvictor, recorder);
	}

	@Bean
	public HibernateCommitSignalListener hibernateCommitSignalListener(
		 CommitSignalSink sink,
		 InvalidationRecorder recorder
	) {
		return new HibernateCommitSignalListener(sink, recorder);
	}

	@Bean
	public SpringCommitSignalListener springCommitSignalListener(
		 CommitSignalSink sink,
		 InvalidationRecorder recorder
	) {
		return new SpringCommitSignalListener(sink, recorder);
	}

	@Bean
	public HibernateCommitSignalRegistrar hibernateCommitSignalRegistrar(
		 ObjectProvider<EntityManagerFactory> entityManagerFactories,
		 HibernateCommitSignalListener listener
	) {
		return new HibernateCommitSignalRegistrar(entityManagerFactories, listener);
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
