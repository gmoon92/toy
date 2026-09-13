package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import com.gmoon.cacheinvalidation.core.invalidation.change.ChangeSource;

public class InvalidationRecorder {

	private final Map<EvictionKey, LongAdder> evictions = new ConcurrentHashMap<>();
	private final Map<String, LongAdder> ruleFailures = new ConcurrentHashMap<>();
	private final Map<ChangeSource, LongAdder> pipelineFailures = new ConcurrentHashMap<>();

	public record EvictionKey(String cacheName, ChangeSource source, EvictionOutcome outcome) {
	}

	public void recordEviction(String cacheName, ChangeSource source, EvictionOutcome outcome) {
		counterOf(evictions, new EvictionKey(cacheName, source, outcome)).increment();
	}

	public void recordRuleFailure(String ruleName) {
		counterOf(ruleFailures, ruleName).increment();
	}

	public void recordPipelineFailure(ChangeSource source) {
		counterOf(pipelineFailures, source).increment();
	}

	public long evictionCount(String cacheName, ChangeSource source, EvictionOutcome outcome) {
		return counterOf(evictions, new EvictionKey(cacheName, source, outcome)).sum();
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
