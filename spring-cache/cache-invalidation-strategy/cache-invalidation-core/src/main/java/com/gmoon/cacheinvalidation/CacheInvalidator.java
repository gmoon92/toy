package com.gmoon.cacheinvalidation;

import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.cache.CacheManager;

public interface CacheInvalidator {

	/**
	 * @param evictionExecutor 커밋 이후 캐시 삭제를 실행할 곳. {@code Runnable::run} 이면 커밋한 스레드에서 지운다.
	 */
	static CacheInvalidator of(List<InvalidationRule> rules, CacheManager cacheManager, Executor evictionExecutor) {
		AfterCommitEvictor evictor = new AfterCommitEvictor(
			 cacheManager,
			 evictionExecutor
		);
		return new RuleBasedCacheInvalidator(
			 rules,
			 evictor
		);
	}

	void invalidate(EntityChange change);
}
