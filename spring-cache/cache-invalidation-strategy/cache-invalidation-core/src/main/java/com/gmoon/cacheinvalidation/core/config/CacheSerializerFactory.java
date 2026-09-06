package com.gmoon.cacheinvalidation.core.config;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.gmoon.cacheinvalidation.core.cache.CachePolicy;

public class CacheSerializerFactory {

	private final ObjectMapper objectMapper;

	public CacheSerializerFactory() {
		this(defaultObjectMapper());
	}

	public CacheSerializerFactory(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public RedisSerializer<?> serializerFor(CachePolicy policy) {
		return new Jackson2JsonRedisSerializer<>(objectMapper, policy.valueType());
	}

	public RedisSerializer<Object> rejectingSerializer() {
		return new RedisSerializer<>() {

			@Override
			public byte[] serialize(Object value) {
				throw new SerializationException(
					 "No CachePolicy registered for this cache. Register a CachePolicy instead of relying on defaults.");
			}

			@Override
			public Object deserialize(byte[] bytes) {
				throw new SerializationException(
					 "No CachePolicy registered for this cache. Register a CachePolicy instead of relying on defaults.");
			}
		};
	}

	private static ObjectMapper defaultObjectMapper() {
		return new ObjectMapper()
			 .registerModule(new JavaTimeModule())
			 .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
}
