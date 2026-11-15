package com.gmoon.cacheserializer.typed;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 캐시마다 값 타입을 선언하는 JSON 직렬화기를 만든다.
 *
 * <p>값에는 데이터만 실린다. 복원 타입은 읽는 쪽 선언이 정하므로 값이 타입을 바꿀 수 없다.
 * 쓴 쪽에 없던 필드는 무시하고 읽는다.
 */
public final class TypedJson {

	private static final ObjectMapper MAPPER = JsonMapper.builder()
		 .addModule(new JavaTimeModule())
		 .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		 .build();

	private TypedJson() {
	}

	/**
	 * 단일 타입 값을 쓰고 읽는 직렬화기를 만든다.
	 *
	 * @param type 캐시 값 타입. 제네릭 타입이면 {@link #of(TypeReference)} 를 쓴다
	 * @return 값 타입이 고정된 직렬화기
	 */
	public static <T> RedisSerializer<T> of(Class<T> type) {
		return new Jackson2JsonRedisSerializer<>(MAPPER, type);
	}

	/**
	 * 제네릭 타입 값을 쓰고 읽는 직렬화기를 만든다.
	 *
	 * <p>Spring Cache 는 메서드 반환 타입의 제네릭을 캐시에 넘기지 않는다.
	 * {@code List<User>} 같은 값을 {@code List.class} 로 선언하면 원소가 {@code Map} 으로 복원된다.
	 *
	 * @param type 원소 타입까지 담은 캐시 값 타입
	 * @return 값 타입이 고정된 직렬화기
	 * @see <a href="https://github.com/spring-projects/spring-data-redis/issues/2374">spring-data-redis #2374</a>
	 */
	public static <T> RedisSerializer<T> of(TypeReference<T> type) {
		JavaType javaType = MAPPER.getTypeFactory().constructType(type);
		return new Jackson2JsonRedisSerializer<>(MAPPER, javaType);
	}
}
