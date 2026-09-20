package com.gmoon.cacheinvalidation.config;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gmoon.cacheinvalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.EntityChange;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class PublishedEntityChangeListener {

	private final CacheInvalidator cacheInvalidator;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onEntityChanged(EntityChange change) {
		cacheInvalidator.invalidate(change);
	}
}
