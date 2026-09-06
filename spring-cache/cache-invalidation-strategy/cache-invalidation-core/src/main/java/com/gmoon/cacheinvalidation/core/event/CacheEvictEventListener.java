package com.gmoon.cacheinvalidation.core.event;

import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.gmoon.cacheinvalidation.core.invalidation.EntityChange;
import com.gmoon.cacheinvalidation.core.invalidation.EntityChangeInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.PreviousState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CacheEvictEventListener
	 implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener {

	private final EntityChangeInvalidator invalidator;

	@Override
	public void onPostInsert(PostInsertEvent event) {
		invalidator.invalidate(EntityChange.inserted(event.getEntity(), event.getId()));
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		invalidator.invalidate(EntityChange.updated(
			 event.getEntity(),
			 event.getId(),
			 PreviousState.of(event.getPersister().getPropertyNames(), event.getOldState())));
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		invalidator.invalidate(EntityChange.deleted(event.getEntity(), event.getId()));
	}

	@Override
	public boolean requiresPostCommitHandling(EntityPersister persister) {
		return true;
	}

	@Override
	public void onPostInsertCommitFailed(PostInsertEvent event) {
		logCommitFailed(event.getEntity());
	}

	@Override
	public void onPostUpdateCommitFailed(PostUpdateEvent event) {
		logCommitFailed(event.getEntity());
	}

	@Override
	public void onPostDeleteCommitFailed(PostDeleteEvent event) {
		logCommitFailed(event.getEntity());
	}

	private void logCommitFailed(Object entity) {
		log.debug("commit failed. cache eviction skipped. entity: {}", entity.getClass().getSimpleName());
	}
}
