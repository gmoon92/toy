package com.gmoon.cacheinvalidation.core.serialization;

import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.core.policy.CachePolicy;

public interface SerializerFactory {

	RedisSerializer<?> valueSerializerFor(CachePolicy policy);

	RedisSerializer<?> unregisteredCacheSerializer();

	default RedisSerializer<String> keySerializer() {
		return RedisSerializer.string();
	}
}
