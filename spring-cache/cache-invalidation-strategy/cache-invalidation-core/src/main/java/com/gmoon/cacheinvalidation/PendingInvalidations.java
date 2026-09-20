package com.gmoon.cacheinvalidation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.gmoon.cacheinvalidation.EntityChange.Source;

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
 */
final class PendingInvalidations implements TransactionSynchronization {

	private static final Object RESOURCE_KEY = PendingInvalidations.class;

	private final Map<CacheKey, Source> entries = new LinkedHashMap<>();
	private final Consumer<Map<CacheKey, Source>> eviction;

	private PendingInvalidations(Consumer<Map<CacheKey, Source>> eviction) {
		this.eviction = eviction;
	}

	static void collect(Map<CacheKey, Source> entries, Consumer<Map<CacheKey, Source>> eviction) {
		if (entries.isEmpty()) {
			return;
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			eviction.accept(entries);
			return;
		}
		boundTo(eviction).merge(entries);
	}

	private static PendingInvalidations boundTo(Consumer<Map<CacheKey, Source>> eviction) {
		PendingInvalidations pending =
			 (PendingInvalidations)TransactionSynchronizationManager.getResource(RESOURCE_KEY);

		if (pending == null) {
			pending = new PendingInvalidations(eviction);
			TransactionSynchronizationManager.bindResource(RESOURCE_KEY, pending);
			TransactionSynchronizationManager.registerSynchronization(pending);
		}
		return pending;
	}

	/**
	 * 같은 키를 여러 소스가 지목하면 먼저 도착한 소스로 기록한다.
	 * <p>
	 * 한 신호원만 지목한 키는 그 신호원으로 정확히 귀속된다.
	 * 여러 신호원이 같은 키를 지목하는 것은 같은 변경을 중복으로 알린 경우이고,
	 * 그때의 귀속은 프레임워크의 단계 순서를 따르므로 의미를 두지 않는다.
	 */
	private void merge(Map<CacheKey, Source> more) {
		more.forEach(entries::putIfAbsent);
	}

	@Override
	public void afterCompletion(int status) {
		TransactionSynchronizationManager.unbindResourceIfPossible(RESOURCE_KEY);

		if (status == STATUS_COMMITTED) {
			eviction.accept(entries);
		}
	}
}
