package com.gmoon.cacheinvalidation.core.event;

import java.util.function.Supplier;

import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.gmoon.cacheinvalidation.core.invalidation.CommitSignalSink;
import com.gmoon.cacheinvalidation.core.invalidation.EntityChange;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationSource;
import com.gmoon.cacheinvalidation.core.invalidation.PreviousState;
import com.gmoon.cacheinvalidation.core.resilience.InvalidationRecorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class HibernateCommitSignalListener
	 implements PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener {

	private static final InvalidationSource SOURCE = InvalidationSource.HIBERNATE_POST_COMMIT;

	private final CommitSignalSink sink;
	private final InvalidationRecorder recorder;

	@Override
	public void onPostInsert(PostInsertEvent event) {
		signalWithoutDisruptingCommit(() -> EntityChange.inserted(event.getEntity(), event.getId()));
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		signalWithoutDisruptingCommit(() -> EntityChange.updated(
			 event.getEntity(),
			 event.getId(),
			 PreviousState.of(event.getPersister().getPropertyNames(), event.getOldState())));
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		signalWithoutDisruptingCommit(() -> EntityChange.deleted(event.getEntity(), event.getId()));
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

	private void signalWithoutDisruptingCommit(Supplier<EntityChange> change) {
		try {
			sink.accept(change.get(), SOURCE);
		} catch (RuntimeException e) {
			recorder.recordPipelineFailure(SOURCE);
			log.warn("cache invalidation failed after commit. the transaction stays committed", e);
		}
	}

	private void logCommitFailed(Object entity) {
		log.debug("commit failed. cache eviction skipped. entity: {}", entity.getClass().getSimpleName());
	}
}
