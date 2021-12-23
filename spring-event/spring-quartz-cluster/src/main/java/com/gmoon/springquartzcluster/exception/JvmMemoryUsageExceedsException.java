package com.gmoon.springquartzcluster.exception;

public class JvmMemoryUsageExceedsException extends RuntimeException {
	public JvmMemoryUsageExceedsException() {
		super("JVM 메모리 여유 공간이 부족합니다.");
	}
}
