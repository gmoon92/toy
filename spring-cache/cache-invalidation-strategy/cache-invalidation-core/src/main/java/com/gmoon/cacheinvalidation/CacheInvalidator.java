package com.gmoon.cacheinvalidation;

import java.util.List;

import org.springframework.cache.CacheManager;

public interface CacheInvalidator {

	/**
	 * 규칙 기반 무효화를, 실패를 삼키는 껍데기로 감싸 조립한다.
	 * <p>
	 * 신호는 모두 커밋 이후에 도착하므로 파이프라인 밖으로 예외가 새면 안 된다.
	 */
	static CacheInvalidator failSafe(
		 List<InvalidationRule> rules,
		 CacheManager cacheManager,
		 InvalidationRecorder recorder
	) {
		return new FailSafeCacheInvalidator(
			 new DefaultCacheInvalidator(rules, cacheManager, recorder), recorder);
	}

	void invalidate(EntityChange change);
}
