package com.gmoon.cacheinvalidation.config;

import java.util.function.Supplier;

import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.gmoon.cacheinvalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.EntityChange;
import com.gmoon.cacheinvalidation.InvalidationRecorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class JpaEntityChangeListener
	 implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener {

	private final CacheInvalidator cacheInvalidator;
	private final InvalidationRecorder recorder;

	@Override
	public void onPostInsert(PostInsertEvent event) {
		invalidate(() -> EntityChange.committed(event.getEntity(), EntityChange.Type.INSERT, null));
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		invalidate(() -> EntityChange.committed(
			 event.getEntity(),
			 EntityChange.Type.UPDATE,
			 EntityChange.State.of(event.getPersister().getPropertyNames(), event.getOldState())));
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		invalidate(() -> EntityChange.committed(event.getEntity(), EntityChange.Type.DELETE, null));
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
			recorder.recordPipelineFailure(EntityChange.Source.JPA_ENTITY);
			log.warn("failed to read the committed entity state. cache eviction skipped", e);
			return;
		}
		cacheInvalidator.invalidate(read);
	}

	private void logCommitFailed(Object entity) {
		log.debug("commit failed. cache eviction skipped. entity: {}", entity.getClass().getSimpleName());
	}
}
