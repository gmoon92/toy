package com.gmoon.cacheinvalidation.core.config.listener;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.event.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.metrics.InvalidationRecorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.gmoon.cacheinvalidation.core.invalidation.event.EntityChange;

@Slf4j
@RequiredArgsConstructor
public class PublishedEntityChangeListener {

	private static final ChangeSource SOURCE = ChangeSource.APPLICATION_EVENT;

	private final CacheInvalidator cacheInvalidator;
	private final InvalidationRecorder recorder;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onEntityChanged(EntityChange change) {
		try {
			cacheInvalidator.invalidate(change, SOURCE);
		} catch (RuntimeException e) {
			recorder.recordPipelineFailure(SOURCE);
			log.warn("cache invalidation failed after commit. the transaction stays committed", e);
		}
	}
}
