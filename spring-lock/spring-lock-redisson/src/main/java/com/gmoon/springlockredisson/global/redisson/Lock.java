package com.gmoon.springlockredisson.global.redisson;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Lock implements Serializable {

	private static final long serialVersionUID = -8119004717951615799L;

	@EqualsAndHashCode.Include
	private final LockKey key;
	private final RLock value;

	public boolean tryLock() throws InterruptedException {
		long waitTime = key.getWaitTimeSeconds();
		long leaseTime = key.getLeaseTimeSeconds();
		return value.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
	}

	public void release() {
		if (value.isLocked() && value.isHeldByCurrentThread()) {
			value.unlock();
		}
	}

	public String getKey() {
		return key.getKey();
	}

	public boolean isAlive() {
		return value.remainTimeToLive() > 0;
	}
}
