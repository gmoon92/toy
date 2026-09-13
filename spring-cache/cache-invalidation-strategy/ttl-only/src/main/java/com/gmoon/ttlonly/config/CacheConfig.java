package com.gmoon.ttlonly.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;

import com.gmoon.cacheinvalidation.core.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.config.AbstractRedisCacheConfig;
import com.gmoon.ttlonly.user.UserCachePolicy;

/**
 * 무효화 신호 소스를 선언하지 않는다.
 * 변경을 캐시에 전파하는 경로가 없으므로 최신성은 TTL 만료로만 회복된다.
 */
@Configuration
public class CacheConfig extends AbstractRedisCacheConfig {

	@Override
	protected List<CachePolicy> cachePolicies() {
		return List.of(UserCachePolicy.values());
	}
}
