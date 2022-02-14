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
class RedisClientTest {
	final RedisClient redisClient;

	@BeforeEach
	void setUp() {
		redisClient.deleteAll();
	}

	@Test
	void testSave() {
		// given
		String key = "hello";
		String value = "redis";
		long ttl = 3;
		Cache cache = Cache.create(key, Duration.ofSeconds(ttl));

		// when
		redisClient.save(cache, value);

		// then
		assertAll(
			() -> assertThat(redisClient.getTTL(cache.getKey()))
				.isInstanceOf(Long.class)
				.isEqualTo(ttl),
			() -> {
				String actual = redisClient.find(cache.getKey());
				assertThat(actual).isEqualTo(value);
			}
		);
	}

	@Test
	void testFind() {
		// given
		Cache cache = Cache.create("hello");
		String value = "redis";
		redisClient.save(cache, value);

		// when
		String actual = redisClient.find(cache.getKey());

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
			.isThrownBy(() -> redisClient.getOrElseThrow(key));
	}

	@Test
	@DisplayName("TTL 설정 무효화"
		+ " 만료되지 않는 key 로 설정 후 조회")
	void testGetAndChangedByEternalKey() {
		// given
		Cache cache = Cache.create("hello", Duration.ofSeconds(10));
		redisClient.save(cache, "redis");

		// when
		redisClient.getAndChangedByEternalKey(cache.getKey());

		// then
		assertThat(redisClient.getTTL(cache.getKey())).isEqualTo(-1);
	}

	@Test
	@DisplayName("TTL 설정 변경")
	void testChangeTTL() {
		// given
		Cache cache = Cache.create("hello");
		redisClient.save(cache, "redis");

		// when
		Duration ttl = Duration.ofSeconds(10);
		redisClient.changeTTL(cache.getKey(), ttl);

		// then
		assertThat(ttl)
			.isEqualTo(Duration.ofSeconds(redisClient.getTTL(cache.getKey())));
	}

	@Test
	void testDelete() {
		// given
		Cache cache = Cache.create("TEST");
		redisClient.save(cache, UUID.randomUUID().toString());

		// when then
		assertAll(
			() -> assertThat(redisClient.delete(cache.getKey())).isTrue(),
			() -> assertThat(redisClient.delete(cache.getKey())).isFalse()
		);
	}

	@Test
	@DisplayName("정규식으로 등록된 캐시 키 조회")
	void testKeys() {
		// given
		redisClient.save(Cache.create("gmoon1"), "value");
		redisClient.save(Cache.create("gmoon2"), "value");

		// when
		Set<String> keys = redisClient.keys(Cache.CACHE_KEY_STORAGE + ":gmoon*");

		// then
		assertThat(keys).hasSize(2);
	}
}
