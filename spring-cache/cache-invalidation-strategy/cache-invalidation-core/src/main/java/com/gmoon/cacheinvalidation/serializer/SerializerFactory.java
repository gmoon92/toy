package com.gmoon.cacheinvalidation.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.CachePolicy;

public interface SerializerFactory {

	static SerializerFactory json() {
		return new JsonSerializerFactory();
	}

	RedisSerializer<?> valueSerializerFor(CachePolicy policy);

	default RedisSerializer<String> keySerializer() {
		return RedisSerializer.string();
	}
}
