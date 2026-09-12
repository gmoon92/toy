package com.gmoon.cacheinvalidation.core.cache.serialization;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.core.cache.CachePolicy;
import org.springframework.data.redis.serializer.SerializationException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonCacheSerialization implements CacheSerialization {

	private final ObjectMapper objectMapper;

	public JsonCacheSerialization() {
		this(defaultObjectMapper());
	}

	public JsonCacheSerialization(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public RedisSerializer<?> valueSerializerFor(CachePolicy policy) {
		return new Jackson2JsonRedisSerializer<>(objectMapper, policy.valueType());
	}

	@Override
	public RedisSerializer<?> unregisteredCacheSerializer() {
		return new UnregisteredCacheSerializer();
	}

	public static ObjectMapper defaultObjectMapper() {
		return new ObjectMapper()
			 .registerModule(new JavaTimeModule())
			 .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	static final class UnregisteredCacheSerializer implements RedisSerializer<Object> {

		private static final String MESSAGE =
			 "No CachePolicy registered for this cache. Register a CachePolicy instead of relying on defaults.";

		@Override
		public byte[] serialize(Object value) {
			throw new SerializationException(MESSAGE);
		}

		@Override
		public Object deserialize(byte[] bytes) {
			throw new SerializationException(MESSAGE);
		}
	}
}
