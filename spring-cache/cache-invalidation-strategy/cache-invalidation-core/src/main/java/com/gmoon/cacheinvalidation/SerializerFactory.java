package com.gmoon.cacheinvalidation;

import org.springframework.data.redis.serializer.RedisSerializer;

public interface SerializerFactory {

	static SerializerFactory json() {
		return new JsonSerializerFactory();
	}

	RedisSerializer<?> valueSerializerFor(CachePolicy policy);

	RedisSerializer<?> unregisteredCacheSerializer();

	default RedisSerializer<String> keySerializer() {
		return RedisSerializer.string();
	}
}
