package com.gmoon.cacheserializer.jdk;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;

import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 허용 목록에 있는 클래스만 역직렬화하는 JDK 직렬화기를 만든다.
 *
 * <p>JDK 역직렬화는 값이 지시하는 대로 객체 그래프를 재생하며 {@code readObject} 같은 훅을 실행한다.
 * 이 캐시에만 필터를 걸어 JVM 전역 설정 없이 그 경로를 닫는다.
 */
public final class FilteredJdk {

	private static final String DENY_ALL_OTHERS = "!*";

	private FilteredJdk() {
	}

	/**
	 * 허용 패턴 밖의 클래스를 모두 거부하는 직렬화기를 만든다.
	 *
	 * <p>패턴 끝에 {@code !*} 를 붙여 기본 거부로 만든다.
	 * 판정은 클래스 기술자를 읽는 시점이라 {@code readObject} 훅이 실행되기 전이다.
	 *
	 * @param patterns {@link ObjectInputFilter.Config#createFilter(String)} 형식의 허용 패턴.
	 *     예: {@code com.example.order.**}, {@code java.util.*}
	 * @return 역직렬화 필터를 건 직렬화기
	 * @throws IllegalArgumentException 패턴을 하나도 주지 않은 경우
	 */
	public static RedisSerializer<Object> allowing(String... patterns) {
		Assert.notEmpty(patterns, "at least one allowed class pattern is required");

		String allowlist = String.join(";", patterns) + ";" + DENY_ALL_OTHERS;
		ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(allowlist);
		DeserializingConverter deserializer = new DeserializingConverter(filtering(filter));
		return new JdkSerializationRedisSerializer(new SerializingConverter(), deserializer);
	}

	/**
	 * 스트림마다 필터를 건 역직렬화기를 만든다.
	 *
	 * <p>{@code JdkSerializationRedisSerializer} 의 기본 역직렬화기는 스트림에 필터를 걸지 않는다.
	 * 필터는 스트림당 한 번만 설정할 수 있어 스트림을 직접 만들어 건다.
	 *
	 * @param filter 기본 거부로 끝나는 허용 목록 필터
	 * @return 필터를 건 역직렬화기
	 * @see org.springframework.core.serializer.DefaultDeserializer
	 * @see ObjectInputStream#setObjectInputFilter(ObjectInputFilter)
	 */
	private static Deserializer<Object> filtering(ObjectInputFilter filter) {
		return in -> {
			ObjectInputStream stream = new ConfigurableObjectInputStream(in, ClassUtils.getDefaultClassLoader());
			stream.setObjectInputFilter(filter);
			try {
				return stream.readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException("Failed to deserialize cached value", e);
			}
		};
	}
}
