package com.gmoon.cacheinvalidation.core.signal;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gmoon.cacheinvalidation.core.invalidation.CommitSignalSink;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationSource;
import com.gmoon.cacheinvalidation.core.resilience.InvalidationRecorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SpringCommitSignalListener {

	private static final InvalidationSource SOURCE = InvalidationSource.SPRING_AFTER_COMMIT;

	private final CommitSignalSink sink;
	private final InvalidationRecorder recorder;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onEntityChanged(EntityChangedEvent event) {
		try {
			sink.accept(event.change(), SOURCE);
		} catch (RuntimeException e) {
			recorder.recordPipelineFailure(SOURCE);
			log.warn("cache invalidation failed after commit. the transaction stays committed", e);
		}
	}
}
