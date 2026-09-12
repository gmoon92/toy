package com.gmoon.cacheinvalidation.core.listener;

import java.util.function.Supplier;

import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.event.EntityChange;
import com.gmoon.cacheinvalidation.core.event.ChangeSource;
import com.gmoon.cacheinvalidation.core.event.EntityState;
import com.gmoon.cacheinvalidation.core.metrics.InvalidationRecorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JpaEntityChangeListener
	 implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener {

	private static final ChangeSource SOURCE = ChangeSource.JPA_ENTITY;

	private final CacheInvalidator cacheInvalidator;
	private final InvalidationRecorder recorder;

	@Override
	public void onPostInsert(PostInsertEvent event) {
		invalidateWithoutDisruptingCommit(() -> EntityChange.inserted(event.getEntity(), event.getId()));
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		invalidateWithoutDisruptingCommit(() -> EntityChange.updated(
			 event.getEntity(),
			 event.getId(),
			 EntityState.of(event.getPersister().getPropertyNames(), event.getOldState())));
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		invalidateWithoutDisruptingCommit(() -> EntityChange.deleted(event.getEntity(), event.getId()));
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

	private void invalidateWithoutDisruptingCommit(Supplier<EntityChange> change) {
		try {
			cacheInvalidator.invalidate(change.get(), SOURCE);
		} catch (RuntimeException e) {
			recorder.recordPipelineFailure(SOURCE);
			log.warn("cache invalidation failed after commit. the transaction stays committed", e);
		}
	}

	private void logCommitFailed(Object entity) {
		log.debug("commit failed. cache eviction skipped. entity: {}", entity.getClass().getSimpleName());
	}
}
