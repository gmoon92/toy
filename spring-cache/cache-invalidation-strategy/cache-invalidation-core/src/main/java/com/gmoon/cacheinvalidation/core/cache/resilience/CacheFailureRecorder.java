package com.gmoon.cacheinvalidation.core.cache.resilience;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class CacheFailureRecorder {

	private final Map<CacheOperation, LongAdder> failuresByOperation = new ConcurrentHashMap<>();

	public void record(CacheOperation operation) {
		failuresByOperation.computeIfAbsent(operation, ignored -> new LongAdder()).increment();
	}

	public long failureCount(CacheOperation operation) {
		return failuresByOperation.computeIfAbsent(operation, ignored -> new LongAdder()).sum();
	}

	public long totalFailureCount() {
		return failuresByOperation.values()
			 .stream()
			 .mapToLong(LongAdder::sum)
			 .sum();
	}

	public void reset() {
		failuresByOperation.clear();
	}
}
