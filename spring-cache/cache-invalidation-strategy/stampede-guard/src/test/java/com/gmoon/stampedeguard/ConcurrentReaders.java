package com.gmoon.stampedeguard;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * 여러 조회를 같은 순간에 출발시켜, 캐시가 비어 있는 동안 MISS 가 겹치게 만든다.
 */
public final class ConcurrentReaders {

	private ConcurrentReaders() {
	}

	public static <T> List<T> readTogether(int readers, Supplier<T> read) {
		ExecutorService pool = Executors.newFixedThreadPool(readers);
		CountDownLatch startGate = new CountDownLatch(1);
		try {
			List<CompletableFuture<T>> results = IntStream.range(0, readers)
				 .mapToObj(reader -> CompletableFuture.supplyAsync(
					  () -> {
						  await(startGate);
						  return read.get();
					  },
					  pool
				 ))
				 .toList();
			startGate.countDown();
			return results.stream()
				 .map(CompletableFuture::join)
				 .toList();
		} finally {
			pool.shutdownNow();
		}
	}

	public static void pause(Duration duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while pausing", e);
		}
	}

	public static void await(CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting", e);
		}
	}
}
