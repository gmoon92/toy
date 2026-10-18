package com.gmoon.cacheinvalidation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class RuleBasedCacheInvalidator implements CacheInvalidator {

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

	private Set<CacheKey> resolve(EntityChange change) {
		Set<CacheKey> entries = new LinkedHashSet<>();
		for (InvalidationRule rule : rules) {
			entries.addAll(
				 resolveInIsolation(
					  rule,
					  change
				 )
			);
		}
		return entries;
	}

	private Collection<CacheKey> resolveInIsolation(InvalidationRule rule, EntityChange change) {
		try {
			return rule.resolve(change);
		} catch (RuntimeException e) {
			String ruleName = rule.getClass().getSimpleName();
			log.warn(
				 "cache invalidation rule failed. rule: {}",
				 ruleName,
				 e
			);
			metrics.resolutionFailed(ruleName);
			return List.of();
		}
	}
}
