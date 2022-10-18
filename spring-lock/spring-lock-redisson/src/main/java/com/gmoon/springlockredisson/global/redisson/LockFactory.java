package com.gmoon.springlockredisson.global.redisson;

import java.time.Duration;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockFactory {
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
}
