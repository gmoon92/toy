package com.gmoon.writeinvalidate.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gmoon.cacheinvalidation.core.cache.CachePolicies;
import com.gmoon.cacheinvalidation.core.config.CacheInvalidationConfig;
import com.gmoon.writeinvalidate.article.ArticleCachePolicy;
import com.gmoon.writeinvalidate.user.UserCachePolicy;

@Configuration
@Import(CacheInvalidationConfig.class)
public class WriteInvalidateCacheConfig {

	@Bean
	public CachePolicies writeInvalidateCachePolicies() {
		return () -> List.of(UserCachePolicy.USER, ArticleCachePolicy.ARTICLE);
	}
}
