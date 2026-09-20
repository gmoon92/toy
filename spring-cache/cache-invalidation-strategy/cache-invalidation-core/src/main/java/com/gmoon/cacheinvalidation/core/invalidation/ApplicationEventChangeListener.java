package com.gmoon.cacheinvalidation.core.invalidation;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApplicationEventChangeListener {

	private static final ChangeSource SOURCE = ChangeSource.APPLICATION_EVENT;

	private final CacheInvalidator cacheInvalidator;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onEntityChanged(EntityChange change) {
		cacheInvalidator.invalidate(change, SOURCE);
	}
}
