package com.gmoon.ttlonly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gmoon.cacheinvalidation.core.cache.CachePolicies;
import com.gmoon.cacheinvalidation.core.config.CacheInvalidationConfig;
import com.gmoon.ttlonly.user.UserCachePolicy;

@Configuration
@Import(CacheInvalidationConfig.class)
public class CacheConfig {

	@Bean
	public CachePolicies cachePolicies() {
		return CachePolicies.of(UserCachePolicy.values());
	}
}
