package com.gmoon.cacheinvalidation.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

/**
 * 호출 스레드의 트랜잭션에 참여하지 않는 조회를 수행한다.
 * <p>
 * {@code TransactionSynchronizationManager} 는 ThreadLocal 이므로
 * 다른 스레드에서 실행하면 새 트랜잭션이 열리고 커밋된 데이터만 보인다.
 */
public final class SeparateTransaction {

	private SeparateTransaction() {
	}

	public static <T> T read(Supplier<T> query) {
		try {
			return CompletableFuture.supplyAsync(query).join();
		} catch (CompletionException e) {
			throw e.getCause() instanceof RuntimeException cause ? cause : e;
		}
	}
}
