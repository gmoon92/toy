package com.gmoon.outboxinvalidate.cache;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 데이터 변경과 같은 트랜잭션에 무효화 지시를 적는다.
 * <p>
 * 열린 트랜잭션이 없으면 기록을 거부한다.
 * 따로 커밋되면 변경만 남고 지시가 사라지는 틈이 다시 생기기 때문이다.
 */
@Component
@RequiredArgsConstructor
public class EvictionOutbox {

	private final PendingEvictionRepository pendingEvictionRepository;

	@Transactional(propagation = Propagation.MANDATORY)
	public void record(CacheEvictable entity) {
		entity.cacheKeys().stream()
			 .map(PendingEviction::of)
			 .forEach(pendingEvictionRepository::save);
	}
}
