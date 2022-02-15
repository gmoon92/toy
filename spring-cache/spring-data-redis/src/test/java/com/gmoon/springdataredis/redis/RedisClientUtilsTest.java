package com.gmoon.springdataredis.redis;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RedisClientUtilsTest {
	final RedisClientUtils redisClientUtils;

	@BeforeEach
	void setUp() {
		redisClientUtils.deleteAll();
	}

	@Test
	void testSave() {
		// given
		String key = "hello";
		String value = "redis";
		long ttl = 3;
		Cache cache = Cache.create(key, Duration.ofSeconds(ttl));

		// when
		redisClientUtils.save(cache, value);

		// then
		assertAll(
			() -> assertThat(redisClientUtils.getTTL(cache.getKey()))
				.isInstanceOf(Long.class)
				.isEqualTo(ttl),
			() -> {
				String actual = redisClientUtils.find(cache.getKey());
				assertThat(actual).isEqualTo(value);
			}
		);
	}

	@Test
	void testFind() {
		// given
		Cache cache = Cache.create("hello");
		String value = "redis";
		redisClientUtils.save(cache, value);

		// when
		String actual = redisClientUtils.find(cache.getKey());

		// then
		assertThat(actual).isEqualTo(value);
	}

	@Test
	@DisplayName("조회할 Key 가 없다면 예외 발생")
	void testGetOrElseThrow() {
		// given
		String key = LocalDateTime.now() + UUID.randomUUID().toString();

		// when then
		assertThatExceptionOfType(NotFoundDataException.class)
			.isThrownBy(() -> redisClientUtils.getOrElseThrow(key));
	}

	@Test
	@DisplayName("TTL 설정 무효화"
		+ " 만료되지 않는 key 로 설정 후 조회")
	void testGetAndChangedByEternalKey() {
		// given
		Cache cache = Cache.create("hello", Duration.ofSeconds(10));
		redisClientUtils.save(cache, "redis");

		// when
		redisClientUtils.getAndChangedByEternalKey(cache.getKey());

		// then
		assertThat(redisClientUtils.getTTL(cache.getKey())).isEqualTo(-1);
	}

	@Test
	@DisplayName("TTL 설정 변경")
	void testChangeTTL() {
		// given
		Cache cache = Cache.create("hello");
		redisClientUtils.save(cache, "redis");

		// when
		Duration ttl = Duration.ofSeconds(10);
		redisClientUtils.changeTTL(cache.getKey(), ttl);

		// then
		assertThat(ttl)
			.isEqualTo(Duration.ofSeconds(redisClientUtils.getTTL(cache.getKey())));
	}

	@Test
	void testDelete() {
		// given
		Cache cache = Cache.create("TEST");
		redisClientUtils.save(cache, UUID.randomUUID().toString());

		// when then
		assertAll(
			() -> assertThat(redisClientUtils.delete(cache.getKey())).isTrue(),
			() -> assertThat(redisClientUtils.delete(cache.getKey())).isFalse()
		);
	}

	@Test
	@DisplayName("정규식으로 등록된 캐시 키 조회")
	void testKeys() {
		// given
		redisClientUtils.save(Cache.create("gmoon1"), "value");
		redisClientUtils.save(Cache.create("gmoon2"), "value");

		// when
		Set<String> keys = redisClientUtils.keys(Cache.CACHE_KEY_STORAGE + ":gmoon*");

		// then
		assertThat(keys).hasSize(2);
	}
}
