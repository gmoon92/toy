package com.gmoon.outboxinvalidate.cache;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 브로커에서 받은 무효화 지시대로 캐시를 지운다.
 * <p>
 * 캐시 저장소 장애로 던진 예외는 메시지를 큐에 되돌려 다시 받게 한다.
 * 정책에 없는 캐시 이름은 몇 번을 다시 받아도 처리할 수 없으므로 버린다.
 */
@Component
@RequiredArgsConstructor
public class CacheEvictor {

	private final CacheManager cacheManager;

	@RabbitListener(queues = "${service.cache.outbox.queue}")
	public void evict(CacheKey cacheKey) {
		Cache cache = cacheManager.getCache(cacheKey.cacheName());
		if (cache == null) {
			throw new AmqpRejectAndDontRequeueException("Cache not registered: " + cacheKey.cacheName());
		}
		cache.evict(cacheKey.key());
	}
}
