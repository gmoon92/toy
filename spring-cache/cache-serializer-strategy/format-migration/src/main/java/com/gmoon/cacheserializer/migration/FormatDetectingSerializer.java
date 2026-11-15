package com.gmoon.cacheserializer.migration;

import java.io.ObjectStreamConstants;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;

/**
 * 구 포맷(JDK)으로 저장된 값을 새 포맷과 함께 읽는 이관용 직렬화기.
 *
 * <p>쓰기는 새 포맷으로만 한다. 구 값은 읽을 때 새 타입으로 바꿔 돌려주고 {@link #legacyReads()} 로 센다.
 * 이 수가 0 으로 내려오면 이 직렬화기를 걷어낸다.
 *
 * <p>캐시 이름을 올릴 수 있으면 이 직렬화기는 필요 없다. 이름을 바꿀 수 없는 캐시를 위한 수단이다.
 *
 * @param <T> 새 포맷의 값 타입
 */
public final class FormatDetectingSerializer<T> implements RedisSerializer<T> {

	private final RedisSerializer<T> current;
	private final RedisSerializer<Object> legacy;
	private final Function<Object, T> upgrade;
	private final LongAdder legacyReads = new LongAdder();

	/**
	 * @param current 새 포맷 직렬화기. 쓰기는 모두 이쪽으로 간다
	 * @param legacy 구 포맷(JDK) 직렬화기. 역직렬화 필터를 건 것을 넘긴다
	 * @param upgrade 구 포맷에서 읽은 값을 새 타입으로 바꾸는 함수
	 */
	public FormatDetectingSerializer(
		 RedisSerializer<T> current,
		 RedisSerializer<Object> legacy,
		 Function<Object, T> upgrade
	) {
		this.current = current;
		this.legacy = legacy;
		this.upgrade = upgrade;
	}

	@Override
	public byte[] serialize(@Nullable T value) {
		return current.serialize(value);
	}

	@Override
	@Nullable
	public T deserialize(@Nullable byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		if (!isJdkStream(bytes)) {
			return current.deserialize(bytes);
		}
		legacyReads.increment();
		return upgrade.apply(legacy.deserialize(bytes));
	}

	/**
	 * 구 포맷으로 읽은 횟수.
	 *
	 * @return 이 직렬화기를 만든 뒤 구 포맷 값을 읽은 횟수
	 */
	public long legacyReads() {
		return legacyReads.sum();
	}

	/**
	 * 값이 JDK 직렬화 스트림인지 첫 두 바이트로 가른다.
	 *
	 * <p>예외가 나면 구 포맷으로 다시 시도하는 방식은 깨진 값을 구 포맷으로 위장한다.
	 * JSON 은 첫 바이트가 {@code 0xAC} 일 수 없으므로 두 포맷이 겹치지 않는다.
	 *
	 * <p>{@code RedisCache} 는 null 표식을 값 직렬화기를 거치지 않고 JDK 바이트로 따로 쓰고 읽는다.
	 * 그래서 이 판별이 null 표식을 구 포맷 값으로 오인하지 않는다.
	 *
	 * @param bytes 비어 있지 않은 캐시 값
	 * @return JDK 직렬화 스트림이면 {@code true}
	 * @see ObjectStreamConstants#STREAM_MAGIC
	 * @see org.springframework.data.redis.cache.RedisCache#deserializeCacheValue(byte[])
	 */
	private static boolean isJdkStream(byte[] bytes) {
		return bytes.length >= 2
			 && bytes[0] == (byte)(ObjectStreamConstants.STREAM_MAGIC >> 8)
			 && bytes[1] == (byte)ObjectStreamConstants.STREAM_MAGIC;
	}
}
