package com.gmoon.cacheinvalidation.test.measure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import com.gmoon.cacheinvalidation.core.invalidation.EvictionOutcome;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRecorder;
import com.gmoon.cacheinvalidation.core.invalidation.ChangeSource;

/**
 * 무효화 결과를 세는 검증용 구현이다.
 * <p>
 * 제품 코드는 {@link InvalidationRecorder#NONE} 으로 동작하므로,
 * 집계가 필요한 통합 테스트만 이 구현을 주입받는다.
 */
public class InvalidationCounter implements InvalidationRecorder {

	private final Map<Eviction, LongAdder> evictions = new ConcurrentHashMap<>();
	private final Map<String, LongAdder> ruleFailures = new ConcurrentHashMap<>();
	private final Map<ChangeSource, LongAdder> pipelineFailures = new ConcurrentHashMap<>();

	private record Eviction(String cacheName, ChangeSource source, EvictionOutcome outcome) {
	}

	@Override
	public void recordEviction(String cacheName, ChangeSource source, EvictionOutcome outcome) {
		counterOf(evictions, new Eviction(cacheName, source, outcome)).increment();
	}

	@Override
	public void recordRuleFailure(String ruleName) {
		counterOf(ruleFailures, ruleName).increment();
	}

	@Override
	public void recordPipelineFailure(ChangeSource source) {
		counterOf(pipelineFailures, source).increment();
	}

	public long evictionCount(String cacheName, ChangeSource source, EvictionOutcome outcome) {
		return counterOf(evictions, new Eviction(cacheName, source, outcome)).sum();
	}

	public long ruleFailureCount(String ruleName) {
		return counterOf(ruleFailures, ruleName).sum();
	}

	public long pipelineFailureCount(ChangeSource source) {
		return counterOf(pipelineFailures, source).sum();
	}

	public void reset() {
		evictions.clear();
		ruleFailures.clear();
		pipelineFailures.clear();
	}

	private <K> LongAdder counterOf(Map<K, LongAdder> counters, K key) {
		return counters.computeIfAbsent(key, ignored -> new LongAdder());
	}
}
