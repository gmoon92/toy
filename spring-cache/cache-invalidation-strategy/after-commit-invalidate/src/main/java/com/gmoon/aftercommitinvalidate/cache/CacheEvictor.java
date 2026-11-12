package com.gmoon.aftercommitinvalidate.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

/**
 * 발행된 {@link CacheEvictable} 엔티티의 캐시를 커밋이 확정된 뒤에만 지운다.
 * <p>
 * 스프링은 이 단계를 {@code afterCommit()} 이 아니라
 * {@code afterCompletion(STATUS_COMMITTED)} 에서 실행한다
 * (spring-tx {@code TransactionalApplicationListenerSynchronization$PlatformSynchronization}).
 * 그래서 여기서 던진 예외는 커밋한 쪽으로 전파되지 않고 로그만 남는다 — 실패가 조용하다.
 */
@Component
@RequiredArgsConstructor
public class CacheEvictor {

	private final CacheManager cacheManager;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void evict(CacheEvictable entity) {
		entity.cacheKeys().forEach(this::evictOne);
	}

	private void evictOne(CacheKey cacheKey) {
		Cache cache = cacheManager.getCache(cacheKey.cacheName());
		if (cache == null) {
			throw new IllegalStateException("Cache not registered: " + cacheKey.cacheName());
		}
		cache.evict(cacheKey.key());
	}
}
