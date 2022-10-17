package com.gmoon.springlockredisson.global.redisson;

import java.io.Serializable;
import java.time.Duration;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class LockKey implements Serializable {

	private static final long serialVersionUID = -94383080006313306L;
	private static final String PREFIX = "rlk-";

	@Getter
	@EqualsAndHashCode.Include
	private final String key;
	private final Duration waitTime;
	private final Duration leaseTime;

	protected LockKey(String key, Duration waitTime, Duration leaseTime) {
		this.key = PREFIX + key;
		this.waitTime = waitTime;
		this.leaseTime = leaseTime;
	}

	public long getWaitTimeSeconds() {
		return waitTime.getSeconds();
	}

	public long getLeaseTimeSeconds() {
		return leaseTime.getSeconds();
	}
}
