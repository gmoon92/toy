package com.gmoon.cacheinvalidation.core.serialization;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gmoon.cacheinvalidation.core.policy.CachePolicy;

class JsonSerializerFactory implements SerializerFactory {

	private final ObjectMapper objectMapper;

	JsonSerializerFactory() {
		this.objectMapper = defaultObjectMapper();
	}

	@Override
	public RedisSerializer<?> valueSerializerFor(CachePolicy policy) {
		return new Jackson2JsonRedisSerializer<>(objectMapper, policy.valueType());
	}

	@Override
	public RedisSerializer<?> unregisteredCacheSerializer() {
		return new UnregisteredCacheSerializer();
	}

	private static ObjectMapper defaultObjectMapper() {
		return new ObjectMapper()
			 .registerModule(new JavaTimeModule())
			 .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	private static final class UnregisteredCacheSerializer implements RedisSerializer<Object> {

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
