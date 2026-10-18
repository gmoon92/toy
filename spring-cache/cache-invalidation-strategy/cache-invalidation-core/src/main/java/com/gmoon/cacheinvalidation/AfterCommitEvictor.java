package com.gmoon.cacheinvalidation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link TransactionSynchronization} 은 트랜잭션이 살아 있을 때만 등록할 수 있다.
 * 커밋 이후에 도착하는 신호만으로는 수집함을 만들 수 없으므로 신호는 트랜잭션 안에서 받는다.
 * <p>
 * 스프링은 트랜잭션을 중단할 때 {@code bindResource} 로 묶은 리소스를 떼 주지 않는다.
 * 수집함이 {@link TransactionSynchronization#suspend()} 에서 직접 떼지 않으면
 * {@code REQUIRES_NEW} 안쪽이 바깥의 수집함에 모은다.
 * <p>
 * 실행기로 넘긴 삭제는 아직 끝난 것이 아니다. 넘긴 것을 {@link #drainPending()} 가 볼 수 있도록
 * 들고 있다가, 애플리케이션이 내려갈 때 남은 것을 그 스레드에서 마저 지운다.
 * 실행기 종료를 기다리지 않고 직접 지우므로, 실행기가 먼저 멈춰도 유실되지 않는다.
 */
@Slf4j
@RequiredArgsConstructor
final class AfterCommitEvictor {

	private final CacheManager cacheManager;
	private final Executor executor;
	private final InvalidationMetrics metrics;

	private final Map<Object, List<CacheKey>> handedOff = new ConcurrentHashMap<>();

	void evict(Set<CacheKey> cacheKeys) {
		if (cacheKeys.isEmpty()) {
			return;
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			dispatch(cacheKeys);
			return;
		}
		pendingOfCurrentTransaction().merge(cacheKeys);
	}

	private Pending pendingOfCurrentTransaction() {
		Pending pending = (Pending)TransactionSynchronizationManager.getResource(this);
		if (pending == null) {
			pending = new Pending();
			pending.bind();
			TransactionSynchronizationManager.registerSynchronization(pending);
		}
		return pending;
	}

	/**
	 * 실행기로 넘기지 못하고 남은 삭제를 호출한 스레드에서 마저 수행한다.
	 * <p>
	 * 실행기를 비동기로 바꾸면 프로세스가 내려갈 때 큐에 남은 삭제가 사라진다.
	 * 거부({@link RejectedExecutionException})와 달리 그 자리에서 알 수 없으므로
	 * 넘긴 것을 들고 있다가 종료 시점에 훑는다.
	 */
	void drainPending() {
		handedOff.keySet().forEach(this::evictIfNotTakenYet);
	}

	private void dispatch(Collection<CacheKey> cacheKeys) {
		List<CacheKey> snapshot = List.copyOf(cacheKeys);
		Object ticket = new Object();
		handedOff.put(ticket, snapshot);

		try {
			executor.execute(() -> evictIfNotTakenYet(ticket));
		} catch (RejectedExecutionException e) {
			log.warn("cache eviction rejected by executor. evicting on the caller thread", e);
			evictIfNotTakenYet(ticket);
		}
	}

	/**
	 * 같은 삭제를 실행기와 종료 처리가 동시에 집어도 한쪽만 수행한다.
	 */
	private void evictIfNotTakenYet(Object ticket) {
		List<CacheKey> taken = handedOff.remove(ticket);
		if (taken != null) {
			evictEach(taken);
		}
	}

	private void evictEach(List<CacheKey> cacheKeys) {
		cacheKeys.forEach(this::evictOne);
	}

	private void evictOne(CacheKey cacheKey) {
		try {
			Cache cache = cacheManager.getCache(cacheKey.cacheName());
			if (cache == null) {
				log.warn("cache not registered. name: {}", cacheKey.cacheName());
				metrics.evictionFailed(cacheKey.cacheName());
				return;
			}
			cache.evict(cacheKey.key());
		} catch (RuntimeException e) {
			log.warn("cache eviction failed. key: {}", cacheKey, e);
			metrics.evictionFailed(cacheKey.cacheName());
		}
	}

	private final class Pending implements TransactionSynchronization {

		private final Set<CacheKey> cacheKeys = new LinkedHashSet<>();

		private void merge(Set<CacheKey> more) {
			cacheKeys.addAll(more);
		}

		private void bind() {
			TransactionSynchronizationManager.bindResource(AfterCommitEvictor.this, this);
		}

		private void unbind() {
			TransactionSynchronizationManager.unbindResourceIfPossible(AfterCommitEvictor.this);
		}

		@Override
		public void suspend() {
			unbind();
		}

		@Override
		public void resume() {
			bind();
		}

		@Override
		public void afterCompletion(int status) {
			unbind();
			if (status == STATUS_COMMITTED) {
				dispatch(cacheKeys);
			}
		}
	}
}
