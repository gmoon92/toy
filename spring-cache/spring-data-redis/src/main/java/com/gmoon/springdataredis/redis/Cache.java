package com.gmoon.springdataredis.redis;

import java.io.Serializable;
import java.time.Duration;

import org.springframework.util.Assert;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = "key")
public class Cache implements Serializable {
	public static final String CACHE_KEY_STORAGE = "CACHE_KEY_STORAGE";

	private static final Duration TTL_OF_DEFAULT = Duration.ofHours(1);

	private final String key;
	private final Duration ttl;

	private Cache(String key, Duration ttl) {
		Assert.notNull(key, "cache key is null.");
		this.key = String.format("%s:%s", CACHE_KEY_STORAGE, key);
		this.ttl = ttl;
	}

	public static Cache create(String key) {
		return create(key, TTL_OF_DEFAULT);
	}

	public static Cache create(String key, Duration ttl) {
		return new Cache(key, ttl);
	}
}
