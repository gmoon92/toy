package com.gmoon.cacheserializer.polymorphic;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 값 안의 {@code @class} 로 복원 타입을 고르는 JSON 직렬화기를 만든다.
 *
 * <p>값 타입을 선언하지 않아도 되는 대신, 값이 복원할 클래스를 지목한다.
 * 허용 목록 밖의 클래스는 만들기 전에 거부한다.
 */
public final class PolymorphicJson {

	private PolymorphicJson() {
	}

	/**
	 * 허용한 접두사의 클래스만 복원하는 직렬화기를 만든다.
	 *
	 * @param allowedPrefixes 복원을 허용할 클래스 이름 접두사.
	 *     컬렉션과 시간 타입도 값에 클래스 이름이 실리므로 {@code java.util.}, {@code java.time.} 을 함께 적는다
	 * @return 허용 목록을 건 직렬화기
	 * @throws IllegalArgumentException 접두사를 하나도 주지 않은 경우
	 */
	public static RedisSerializer<Object> allowing(String... allowedPrefixes) {
		Assert.notEmpty(allowedPrefixes, "at least one allowed class name prefix is required");

		ObjectMapper mapper = JsonMapper.builder()
			 .addModule(new JavaTimeModule())
			 .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			 .polymorphicTypeValidator(allowlistOf(allowedPrefixes))
			 .build();
		return withDefaultTyping(mapper);
	}

	private static PolymorphicTypeValidator allowlistOf(String[] allowedPrefixes) {
		BasicPolymorphicTypeValidator.Builder allowlist = BasicPolymorphicTypeValidator.builder();
		for (String prefix : allowedPrefixes) {
			allowlist.allowIfSubType(prefix);
		}
		return allowlist.build();
	}

	/**
	 * 넘긴 {@code ObjectMapper} 에 기본 타이핑을 켜서 직렬화기를 만든다.
	 *
	 * <p>{@link GenericJackson2JsonRedisSerializer#builder()} 는 {@code ObjectMapper} 를 넘기면 기본 타이핑을 켜지 않는다.
	 * {@code defaultTyping(true)} 를 빠뜨리면 값에 {@code @class} 가 실리지 않고 예외도 나지 않는다.
	 *
	 * <p>켜진 타이핑은 넘긴 {@code ObjectMapper} 의 {@link PolymorphicTypeValidator} 를 그대로 쓴다.
	 * Jackson 의 기본 검증기는 모든 클래스를 허용하므로 허용 목록은 여기서 건다.
	 *
	 * @param mapper 허용 목록을 건 {@code ObjectMapper}
	 * @return 모든 값에 {@code @class} 를 싣는 직렬화기
	 * @see GenericJackson2JsonRedisSerializer.GenericJackson2JsonRedisSerializerBuilder#build()
	 * @see com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
	 */
	private static RedisSerializer<Object> withDefaultTyping(ObjectMapper mapper) {
		return GenericJackson2JsonRedisSerializer.builder()
			 .objectMapper(mapper)
			 .defaultTyping(true)
			 .build();
	}
}
