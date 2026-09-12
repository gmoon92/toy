package com.gmoon.cacheinvalidation.core.resilience;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import com.gmoon.cacheinvalidation.core.cache.EvictionOutcome;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationSource;

public class InvalidationRecorder {

	private final Map<EvictionKey, LongAdder> evictions = new ConcurrentHashMap<>();
	private final Map<String, LongAdder> ruleFailures = new ConcurrentHashMap<>();
	private final Map<InvalidationSource, LongAdder> pipelineFailures = new ConcurrentHashMap<>();

	public record EvictionKey(String cacheName, InvalidationSource source, EvictionOutcome outcome) {
	}

	public void recordEviction(String cacheName, InvalidationSource source, EvictionOutcome outcome) {
		counterOf(evictions, new EvictionKey(cacheName, source, outcome)).increment();
	}

	public void recordRuleFailure(String ruleName) {
		counterOf(ruleFailures, ruleName).increment();
	}

	public void recordPipelineFailure(InvalidationSource source) {
		counterOf(pipelineFailures, source).increment();
	}

	public long evictionCount(String cacheName, InvalidationSource source, EvictionOutcome outcome) {
		return counterOf(evictions, new EvictionKey(cacheName, source, outcome)).sum();
	}

	public long ruleFailureCount(String ruleName) {
		return counterOf(ruleFailures, ruleName).sum();
	}

	public long pipelineFailureCount(InvalidationSource source) {
		return counterOf(pipelineFailures, source).sum();
	}

	public Map<EvictionKey, Long> evictionSnapshot() {
		return snapshotOf(evictions);
	}

	public Map<String, Long> ruleFailureSnapshot() {
		return snapshotOf(ruleFailures);
	}

	public Map<InvalidationSource, Long> pipelineFailureSnapshot() {
		return snapshotOf(pipelineFailures);
	}

	public void reset() {
		evictions.clear();
		ruleFailures.clear();
		pipelineFailures.clear();
	}

	private <K> LongAdder counterOf(Map<K, LongAdder> counters, K key) {
		return counters.computeIfAbsent(key, ignored -> new LongAdder());
	}

	private <K> Map<K, Long> snapshotOf(Map<K, LongAdder> counters) {
		return counters.entrySet()
			 .stream()
			 .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().sum()));
	}
}
