package com.gmoon.springlockredisson.global.redisson;

import java.time.Duration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LockKeyTimePolicy {

	DEFAULT;

	private final Duration waitTime;
	private final Duration leaseTime;

	LockKeyTimePolicy() {
		this(Duration.ofSeconds(10), Duration.ofSeconds(10));
	}
}
