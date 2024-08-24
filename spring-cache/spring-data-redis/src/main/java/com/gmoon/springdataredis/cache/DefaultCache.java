package com.gmoon.springdataredis.cache;

import java.time.Duration;

import org.springframework.util.Assert;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DefaultCache implements Cache {
	private static final String CACHE_KEY_STORAGE = "CACHE_KEY_STORAGE";
	private static final Duration TTL_OF_DEFAULT = Duration.ofHours(1);

	@EqualsAndHashCode.Include
	private final String key;
	private final Duration ttl;

	private DefaultCache(String key, Duration ttl) {
		Assert.notNull(key, "cache key is null.");
		this.key = String.format("%s:%s", CACHE_KEY_STORAGE, key);
		this.ttl = ttl;
	}

	public static DefaultCache create(String key) {
		return create(key, TTL_OF_DEFAULT);
	}

	public static DefaultCache create(String key, Duration ttl) {
		return new DefaultCache(key, ttl);
	}
}
