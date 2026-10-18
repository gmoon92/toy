package com.gmoon.cacheinvalidation.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import lombok.RequiredArgsConstructor;

/**
 * 주입된 캐시 관리자가 우리가 설정한 것인지 기동 시점에 확인한다.
 * <p>
 * Boot 의 {@code CacheAutoConfiguration} 은
 * {@code @ConditionalOnMissingBean(value = CacheManager.class, name = "cacheResolver")} 다.
 * Redis 자동설정이 물러나면 Boot 가 대신 다른 제공자를 꽂고, 그러면 이 모듈이 건 설정이
 * <b>통째로 빠진 채로 애플리케이션이 정상 기동한다.</b>
 * <p>
 * 빠지는 것은 TTL·값 타입·미등록 캐시 차단·SCAN 삭제 전부다.
 * 캐시는 동작하는 것처럼 보이고 메모리에 쌓이며 만료되지 않는다.
 * 조용히 성립하는 실패라 기동에서 막는다.
 */
@RequiredArgsConstructor
class CacheManagerValidator implements SmartInitializingSingleton {

	private final CacheManager cacheManager;

	@Override
	public void afterSingletonsInstantiated() {
		if (cacheManager instanceof RedisCacheManager) {
			return;
		}
		throw new IllegalStateException("Expected a RedisCacheManager but got "
			 + cacheManager.getClass().getName() + ". The Redis cache auto-configuration backed off,"
			 + " so this module's ttl, value types and missing-cache guard are not applied."
			 + " Check that a RedisConnectionFactory is available.");
	}
}
