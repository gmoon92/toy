package com.gmoon.cacheinvalidation.core.listener;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.ChangeSource;
import com.gmoon.cacheinvalidation.core.resilience.InvalidationRecorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EntityChangeEventListener {

	private static final ChangeSource SOURCE = ChangeSource.APPLICATION_EVENT;

	private final CacheInvalidator sink;
	private final InvalidationRecorder recorder;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onEntityChanged(EntityChangeEvent event) {
		try {
			sink.invalidate(event.change(), SOURCE);
		} catch (RuntimeException e) {
			recorder.recordPipelineFailure(SOURCE);
			log.warn("cache invalidation failed after commit. the transaction stays committed", e);
		}
	}
}
