package com.gmoon.cacheinvalidation.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.CachePolicy;

public interface SerializerFactory {

	static SerializerFactory json() {
		return new JsonSerializerFactory();
	}

	RedisSerializer<?> valueSerializerFor(CachePolicy policy);

	/**
	 * 정책이 없어 값 타입을 모르는 캐시에 쓴다.
	 * <p>
	 * 타입을 모르므로 타입 정보를 값에 실어야 한다. Boot 기본값인 JDK 직렬화는
	 * 역직렬화가 코드 실행이 되고 {@code Serializable} 이 아닌 타입은 저장조차 못 한다.
	 */
	RedisSerializer<?> untypedValueSerializer();

	default RedisSerializer<String> keySerializer() {
		return RedisSerializer.string();
	}
}
