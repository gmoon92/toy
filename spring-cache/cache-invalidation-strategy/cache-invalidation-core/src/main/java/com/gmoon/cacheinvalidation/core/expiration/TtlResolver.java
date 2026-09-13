package com.gmoon.cacheinvalidation.core.expiration;

import java.time.Duration;

import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * 정책이 선언한 기준 TTL 을 실제 만료 동작으로 해석한다.
 * <p>
 * {@code CachePolicy.ttl()} 은 "이 캐시는 얼마나 살아야 하는가"만 선언하고,
 * 지터를 얹을지·값이 비어 있을 때 다르게 만료시킬지는 여기서 정한다.
 */
public interface TtlResolver {

	RedisCacheWriter.TtlFunction resolveFrom(Duration declaredTtl);
}
