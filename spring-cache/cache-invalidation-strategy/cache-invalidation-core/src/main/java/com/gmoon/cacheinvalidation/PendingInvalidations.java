package com.gmoon.cacheinvalidation;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 한 트랜잭션에서 모인 무효화 대상을 커밋 이후 한 번에 처리한다.
 * <p>
 * 신호원마다 즉시 지우면 같은 변경을 두 경로로 알렸을 때 같은 키를 두 번 지운다.
 * 정합성은 안전하지만 왕복과 지표가 두 배가 된다. 그래서 지우는 자리를 하나로 모았다.
 * <p>
 * 수집은 트랜잭션 안에서 하고 실행은 커밋 이후에 한다.
 * {@link TransactionSynchronization} 은 트랜잭션이 살아 있을 때만 등록할 수 있으므로,
 * 커밋 이후에 도착하는 신호만으로는 이 자리를 만들 수 없다.
 * <p>
 * 트랜잭션 밖에서 들어온 변경은 모으지 않고 그대로 흘려보낸다.
 * <p>
 * {@link #suspend()} 와 {@link #resume()} 은 선택이 아니다.
 * 스프링이 트랜잭션을 중단할 때 {@code bindResource} 로 묶은 리소스는 떼 주지 않으므로,
 * 떼지 않으면 {@code REQUIRES_NEW} 안쪽이 바깥의 수집함에 모으게 된다.
 */
final class PendingInvalidations implements TransactionSynchronization {

	private static final Object RESOURCE_KEY = PendingInvalidations.class;

	private final Set<CacheKey> entries = new LinkedHashSet<>();
	private final Consumer<Set<CacheKey>> eviction;

	private PendingInvalidations(Consumer<Set<CacheKey>> eviction) {
		this.eviction = eviction;
	}

	static void collect(Set<CacheKey> entries, Consumer<Set<CacheKey>> eviction) {
		if (entries.isEmpty()) {
			return;
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			eviction.accept(entries);
			return;
		}
		boundTo(eviction).merge(entries);
	}

	private static PendingInvalidations boundTo(Consumer<Set<CacheKey>> eviction) {
		PendingInvalidations pending =
			 (PendingInvalidations)TransactionSynchronizationManager.getResource(RESOURCE_KEY);

		if (pending == null) {
			pending = new PendingInvalidations(eviction);
			TransactionSynchronizationManager.bindResource(RESOURCE_KEY, pending);
			TransactionSynchronizationManager.registerSynchronization(pending);
		}
		return pending;
	}

	private void merge(Set<CacheKey> more) {
		entries.addAll(more);
	}

	@Override
	public void suspend() {
		TransactionSynchronizationManager.unbindResourceIfPossible(RESOURCE_KEY);
	}

	@Override
	public void resume() {
		TransactionSynchronizationManager.bindResource(
			 RESOURCE_KEY,
			 this
		);
	}

	@Override
	public void afterCompletion(int status) {
		TransactionSynchronizationManager.unbindResourceIfPossible(RESOURCE_KEY);

		if (status == STATUS_COMMITTED) {
			eviction.accept(entries);
		}
	}
}
