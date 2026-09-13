package com.gmoon.cacheinvalidation.core.invalidation.listener;

import java.util.function.Supplier;

import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRecorder;
import com.gmoon.cacheinvalidation.core.invalidation.change.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.change.EntityChange;
import com.gmoon.cacheinvalidation.core.invalidation.change.EntityState;

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
		invalidate(() -> EntityChange.inserted(event.getEntity()));
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		invalidate(() -> EntityChange.updated(
			 event.getEntity(),
			 EntityState.of(event.getPersister().getPropertyNames(), event.getOldState())));
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		invalidate(() -> EntityChange.deleted(event.getEntity()));
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

	private void invalidate(Supplier<EntityChange> change) {
		EntityChange read;
		try {
			read = change.get();
		} catch (RuntimeException e) {
			recorder.recordPipelineFailure(SOURCE);
			log.warn("failed to read the committed entity state. cache eviction skipped", e);
			return;
		}
		cacheInvalidator.invalidate(read, SOURCE);
	}

	private void logCommitFailed(Object entity) {
		log.debug("commit failed. cache eviction skipped. entity: {}", entity.getClass().getSimpleName());
	}
}
