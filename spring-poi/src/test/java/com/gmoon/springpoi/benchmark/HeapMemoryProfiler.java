package com.gmoon.springpoi.benchmark;

import java.util.Collection;
import java.util.Collections;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;

public class HeapMemoryProfiler implements InternalProfiler {
	private long beforeAllocated;

	@Override
	public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
		try {
			System.gc(); // force garbage collection
			Thread.sleep(100);

			beforeAllocated = getUsedMemory();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<? extends Result> afterIteration(
		 BenchmarkParams benchmarkParams,
		 IterationParams iterationParams,
		 IterationResult result
	) {
		long afterAllocated = getUsedMemory();
		long diff = afterAllocated - beforeAllocated;
		double used = diff / 1024.0 / 1024.0;
		System.out.printf("HeapMemoryProfiler: [%s] used: %.2f MB (%,d bytes) (start: %,d, end: %,d)\n%n",
			 benchmarkParams.getBenchmark(),
			 used,
			 diff,
			 beforeAllocated,
			 afterAllocated
		);

		ScalarResult customResult = new ScalarResult(
			 "heap.memory.used.mb",
			 used,
			 "MB",
			 AggregationPolicy.AVG
		);
		return Collections.singleton(customResult);
	}

	private long getUsedMemory() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	@Override
	public String getDescription() {
		return "Custom profiler that measures heap memory increase during each iteration.";
	}
}
