package com.gmoon.cacheinvalidation;

import com.gmoon.cacheinvalidation.EntityChange.Source;

/**
 * 무효화 파이프라인이 결과를 내보내는 쓰기 전용 포트다.
 * <p>
 * 코어는 집계하지 않는다. {@link #none()} 은 아무것도 하지 않으며,
 * 세는 구현은 측정이 필요한 쪽이 제공한다.
 */
public interface InvalidationRecorder {

	enum Outcome {
		EVICT_REQUESTED,
		CACHE_NOT_REGISTERED,
		FAILED
	}

	static InvalidationRecorder none() {
		return NoOp.INSTANCE;
	}

	void recordEviction(String cacheName, Source source, Outcome outcome);

	void recordRuleFailure(String ruleName);

	void recordPipelineFailure(Source source);

	final class NoOp implements InvalidationRecorder {

		private static final NoOp INSTANCE = new NoOp();

		private NoOp() {
		}

		@Override
		public void recordEviction(String cacheName, Source source, Outcome outcome) {
		}

		@Override
		public void recordRuleFailure(String ruleName) {
		}

		@Override
		public void recordPipelineFailure(Source source) {
		}
	}
}
