package com.gmoon.cacheinvalidation.core.fixture;

import java.util.Collection;
import java.util.List;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class FailingCacheManager implements CacheManager {

	private final RuntimeException failure;

	public FailingCacheManager(RuntimeException failure) {
		this.failure = failure;
	}

	public static FailingCacheManager of(String message) {
		return new FailingCacheManager(new IllegalStateException(message));
	}

	@Override
	public Cache getCache(String name) {
		throw failure;
	}

	@Override
	public Collection<String> getCacheNames() {
		return List.of();
	}
}
