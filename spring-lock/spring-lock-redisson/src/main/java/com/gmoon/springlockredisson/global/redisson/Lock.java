package com.gmoon.springlockredisson.global.redisson;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Lock implements Serializable {

	private static final long serialVersionUID = -8119004717951615799L;

	@EqualsAndHashCode.Include
	private final LockKey key;
	private final RLock value;

	public <T> T synchronize(Supplier<T> task) {
		try {
			if (tryLock()) {
				log.info("locking... key: {}", getKeyName());
				return task.get();
			} else {
				throw new LockTimeoutException();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("lock synchronize...", e);
		} catch (LockTimeoutException e) {
			log.info("retry locking...");
			return synchronize(task);
		} finally {
			release();
		}
	}

	public void synchronize(Runnable task) {
		synchronize(() -> {
			task.run();
			return Void.class;
		});
	}

	private boolean tryLock() throws InterruptedException {
		log.info("try lock... key: {}", getKeyName());
		long waitTime = key.getWaitTimeSeconds();
		long leaseTime = key.getLeaseTimeSeconds();
		return value.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
	}

	private void release() {
		if (value.isLocked() && value.isHeldByCurrentThread()) {
			log.info("release lock... key: {}", getKeyName());
			value.unlock();
		}
	}

	private String getKeyName() {
		return key.getKey();
	}
}
