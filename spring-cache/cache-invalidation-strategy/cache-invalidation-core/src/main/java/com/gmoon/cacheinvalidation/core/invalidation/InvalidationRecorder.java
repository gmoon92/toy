package com.gmoon.cacheinvalidation.core.invalidation;

/**
 * 무효화 파이프라인이 결과를 내보내는 쓰기 전용 포트다.
 * <p>
 * 코어는 집계하지 않는다. 기본 구현 {@link #NONE} 은 아무것도 하지 않으며,
 * 세는 구현은 측정이 필요한 쪽이 제공한다.
 */
public interface InvalidationRecorder {

	InvalidationRecorder NONE = new InvalidationRecorder() {

		@Override
		public void recordEviction(String cacheName, ChangeSource source, EvictionOutcome outcome) {
		}

		@Override
		public void recordRuleFailure(String ruleName) {
		}

		@Override
		public void recordPipelineFailure(ChangeSource source) {
		}
	};

	void recordEviction(String cacheName, ChangeSource source, EvictionOutcome outcome);

	void recordRuleFailure(String ruleName);

	void recordPipelineFailure(ChangeSource source);
}
