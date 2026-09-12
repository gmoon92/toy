package com.gmoon.writeinvalidate.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicies;
import com.gmoon.cacheinvalidation.core.config.AbstractCacheConfig;
import com.gmoon.cacheinvalidation.core.config.JpaEntityChangeConfig;
import com.gmoon.cacheinvalidation.core.config.EntityChangeEventConfig;
import com.gmoon.cacheinvalidation.core.invalidation.EvictableEntityRule;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRule;
import com.gmoon.writeinvalidate.article.ArticleCachePolicy;
import com.gmoon.writeinvalidate.user.UserCachePolicy;

/**
 * 두 신호 소스를 모두 켜서 무효화 시점의 차이를 비교한다.
 * {@code ARTICLE} 은 규칙이 없으므로 {@code @CacheEvict} 와 TTL 로만 만료된다.
 */
@Configuration
@Import({JpaEntityChangeConfig.class, EntityChangeEventConfig.class})
public class CacheConfig extends AbstractCacheConfig {

	@Override
	protected CachePolicies cachePolicies() {
		return CachePolicies.of(UserCachePolicy.USER, ArticleCachePolicy.ARTICLE);
	}

	@Override
	protected List<InvalidationRule> invalidationRules() {
		return List.of(EvictableEntityRule.owning(UserCachePolicy.USER));
	}
}
