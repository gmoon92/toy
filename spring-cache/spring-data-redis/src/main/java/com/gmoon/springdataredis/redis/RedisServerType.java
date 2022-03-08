package com.gmoon.springdataredis.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisServerType {
	public static final String STANDALONE = "STANDALONE";
	public static final String SENTINEL = "SENTINEL";
	public static final String ELASTI_CACHE = "ELASTI_CACHE";
}
