package com.gmoon.cacheinvalidation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
 */
@Slf4j
@RequiredArgsConstructor
final class AfterCommitEvictor {

	private final CacheManager cacheManager;
	private final Executor executor;

	void evict(Set<CacheEntry> entries) {
		if (entries.isEmpty()) {
			return;
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			dispatch(entries);
			return;
		}
		pendingOfCurrentTransaction().merge(entries);
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

	private void dispatch(Collection<CacheEntry> entries) {
		List<CacheEntry> snapshot = List.copyOf(entries);
		try {
			executor.execute(() -> evictEach(snapshot));
		} catch (RejectedExecutionException e) {
			log.warn(
				 "cache eviction rejected by executor. evicting on the caller thread",
				 e
			);
			evictEach(snapshot);
		}
	}

	private void evictEach(List<CacheEntry> entries) {
		entries.forEach(this::evictOne);
	}

	private void evictOne(CacheEntry entry) {
		try {
			Cache cache = cacheManager.getCache(entry.cacheName());
			if (cache == null) {
				log.warn(
					 "cache not registered. name: {}",
					 entry.cacheName()
				);
				return;
			}
			cache.evict(entry.key());
		} catch (RuntimeException e) {
			log.warn(
				 "cache eviction failed. entry: {}",
				 entry,
				 e
			);
		}
	}

	private final class Pending implements TransactionSynchronization {

		private final Set<CacheEntry> entries = new LinkedHashSet<>();

		private void merge(Set<CacheEntry> more) {
			entries.addAll(more);
		}

		private void bind() {
			TransactionSynchronizationManager.bindResource(
				 AfterCommitEvictor.this,
				 this
			);
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
				dispatch(entries);
			}
		}
	}
}
