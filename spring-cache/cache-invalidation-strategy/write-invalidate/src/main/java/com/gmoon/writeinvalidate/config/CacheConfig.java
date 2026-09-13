package com.gmoon.writeinvalidate.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gmoon.cacheinvalidation.core.config.AbstractRedisCacheConfig;
import com.gmoon.cacheinvalidation.core.config.ApplicationEventChangeConfig;
import com.gmoon.cacheinvalidation.core.config.JpaEntityChangeConfig;
import com.gmoon.cacheinvalidation.core.invalidation.EvictableEntityRule;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRule;
import com.gmoon.cacheinvalidation.core.invalidation.TtlOnlyRule;
import com.gmoon.cacheinvalidation.core.policy.CachePolicy;
import com.gmoon.writeinvalidate.article.ArticleCachePolicy;
import com.gmoon.writeinvalidate.user.UserCachePolicy;

/**
 * 두 신호 소스를 모두 켜서 무효화 시점의 차이를 비교한다.
 * {@code ARTICLE} 은 {@code @CacheEvict} 와 TTL 로만 만료되므로 규칙이 지우지 않는다.
 */
@Configuration
@Import({JpaEntityChangeConfig.class, ApplicationEventChangeConfig.class})
public class CacheConfig extends AbstractRedisCacheConfig {

	@Override
	protected List<CachePolicy> cachePolicies() {
		return List.of(UserCachePolicy.USER, ArticleCachePolicy.ARTICLE);
	}

	@Override
	protected List<InvalidationRule> invalidationRules() {
		return List.of(
			 EvictableEntityRule.owning(UserCachePolicy.USER),
			 TtlOnlyRule.covering(ArticleCachePolicy.ARTICLE)
		);
	}
}
