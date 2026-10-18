package com.gmoon.cacheinvalidation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.DisposableBean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class RuleBasedCacheInvalidator implements CacheInvalidator, DisposableBean {

	private final List<InvalidationRule> rules;
	private final AfterCommitEvictor evictor;
	private final InvalidationMetrics metrics;

	/**
	 * 무효화 실패는 호출자의 데이터 변경을 되돌리지 않는다.
	 * 신호는 flush 중이거나 커밋 직전, 즉 트랜잭션 안에서 도착하므로
	 * 여기서 예외가 새어나가면 캐시 때문에 트랜잭션이 롤백된다.
	 */
	@Override
	public void invalidate(EntityChange change) {
		try {
			evictor.evict(resolve(change));
		} catch (RuntimeException e) {
			log.warn("cache invalidation failed. the caller's transaction is not affected", e);
		}
	}

	/**
	 * 애플리케이션이 내려갈 때 실행기에 남은 삭제를 마저 수행한다.
	 * <p>
	 * 이 빈은 {@code CacheManager} 에 의존하므로 스프링이 캐시보다 먼저 정리한다.
	 */
	@Override
	public void destroy() {
		evictor.drainPending();
	}

	private Set<CacheKey> resolve(EntityChange change) {
		Set<CacheKey> cacheKeys = new LinkedHashSet<>();
		for (InvalidationRule rule : rules) {
			cacheKeys.addAll(resolveInIsolation(rule, change));
		}
		return cacheKeys;
	}

	private Collection<CacheKey> resolveInIsolation(InvalidationRule rule, EntityChange change) {
		try {
			return rule.resolve(change);
		} catch (RuntimeException e) {
			String ruleName = rule.getClass().getSimpleName();
			log.warn("cache invalidation rule failed. rule: {}", ruleName, e);
			metrics.resolutionFailed(ruleName);
			return List.of();
		}
	}
}
