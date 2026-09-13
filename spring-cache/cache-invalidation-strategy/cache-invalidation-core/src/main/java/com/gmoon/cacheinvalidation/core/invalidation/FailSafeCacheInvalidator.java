package com.gmoon.cacheinvalidation.core.invalidation;

import com.gmoon.cacheinvalidation.core.invalidation.change.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.change.EntityChange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 무효화가 실패해도 호출자에게 예외를 돌려주지 않는다.
 * <p>
 * 신호는 모두 커밋 이후에 도착하므로, 여기서 예외가 새어나가면 이미 끝난 트랜잭션의
 * 호출자가 깨진다. 실패는 기록으로 남기고 캐시는 TTL 이 정리하게 둔다.
 */
@Slf4j
@RequiredArgsConstructor
public class FailSafeCacheInvalidator implements CacheInvalidator {

	private final CacheInvalidator delegate;
	private final InvalidationRecorder recorder;

	@Override
	public void invalidate(EntityChange change, ChangeSource source) {
		try {
			delegate.invalidate(change, source);
		} catch (RuntimeException e) {
			recorder.recordPipelineFailure(source);
			log.warn("cache invalidation failed after commit. the transaction stays committed", e);
		}
	}
}
