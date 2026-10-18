package com.gmoon.cacheinvalidation.serializer;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.gmoon.cacheinvalidation.CachePolicy;

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
	public RedisSerializer<?> untypedValueSerializer() {
		return new GenericJackson2JsonRedisSerializer(defaultObjectMapper());
	}

	private static ObjectMapper defaultObjectMapper() {
		return new ObjectMapper()
			 .registerModule(new JavaTimeModule())
			 .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

}
