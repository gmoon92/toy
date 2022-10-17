package com.gmoon.springlockredisson.global.redisson;

public final class LockTimeoutException extends RuntimeException {

	private static final long serialVersionUID = 1687156046105479551L;

	public LockTimeoutException() {
		super("lock try timeout...");
	}
}
