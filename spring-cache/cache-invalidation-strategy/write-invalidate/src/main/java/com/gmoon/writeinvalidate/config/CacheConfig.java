package com.gmoon.writeinvalidate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gmoon.cacheinvalidation.core.cache.CachePolicies;
import com.gmoon.cacheinvalidation.core.config.CacheInvalidationConfig;
import com.gmoon.cacheinvalidation.core.invalidation.CacheEvictableRule;
import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidationRule;
import com.gmoon.writeinvalidate.article.ArticleCachePolicy;
import com.gmoon.writeinvalidate.user.UserCachePolicy;

@Configuration
@Import(CacheInvalidationConfig.class)
public class CacheConfig {

	@Bean
	public CachePolicies cachePolicies() {
		return CachePolicies.of(UserCachePolicy.USER, ArticleCachePolicy.ARTICLE);
	}

	@Bean
	public CacheInvalidationRule userInvalidationRule() {
		return CacheEvictableRule.owning(UserCachePolicy.USER);
	}
}
