package com.gmoon.springdataredis.cache;

import java.io.Serial;
import java.time.Duration;

import org.springframework.util.Assert;

public record DefaultCache(String key, Duration ttl) implements Cache {

	@Serial
	private static final long serialVersionUID = 2330445518356094990L;

	public DefaultCache {
		Assert.notNull(key, "cache key is null.");
	}
}
