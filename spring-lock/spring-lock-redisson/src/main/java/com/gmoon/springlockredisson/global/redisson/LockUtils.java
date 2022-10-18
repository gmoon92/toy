package com.gmoon.springlockredisson.global.redisson;

import java.time.Duration;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockUtils {
	private final RedissonClient redissonClient;

	public Lock createLock(String key, LockKeyTimePolicy lockKeyTimePolicy) {
		LockKey lockKey = createKey(key, lockKeyTimePolicy);
		RLock lock = redissonClient.getLock(key);
		return new Lock(lockKey, lock);
	}

	private LockKey createKey(String key, LockKeyTimePolicy lockKeyTimePolicy) {
		Duration waitTime = lockKeyTimePolicy.getWaitTime();
		Duration leaseTime = lockKeyTimePolicy.getLeaseTime();
		return new LockKey(key, waitTime, leaseTime);
	}

	public <T> T synchronize(Lock lock, Supplier<T> task) {
		try {
			if (lock.tryLock()) {
				log.info("locking... key: {}", lock.getKey());
				return task.get();
			} else {
				throw new LockTimeoutException();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("lock synchronize...", e);
		} catch (LockTimeoutException e) {
			log.info("retry locking...");
			return synchronize(lock, task);
		} finally {
			lock.release();
		}
	}

	public void synchronize(Lock lock, Runnable task) {
		synchronize(lock, () -> {
			task.run();
			return Void.class;
		});
	}
}
