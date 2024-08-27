package com.gmoon.springdataredis.helper;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import com.gmoon.javacore.util.JacksonUtils;
import com.gmoon.springdataredis.cache.Cache;
import com.gmoon.springdataredis.cache.CachePolicy;
import com.gmoon.springdataredis.cache.DefaultCache;
import com.gmoon.springdataredis.exception.NotFoundDataException;
import com.gmoon.springdataredis.test.ServiceTest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnabledIf(value = "#{'${spring.cache.type}' == 'REDIS'}", loadContext = true)
@ServiceTest
@RequiredArgsConstructor
class RedisTemplateHelperTest {

	private final RedisTemplateHelper helper;

	@BeforeEach
	void clear() {
		helper.evictAll();
	}

	@Test
	void save() {
		var key = "hello";
		var value = "redis";
		var ttl = Duration.ofSeconds(3);
		var cache = new DefaultCache(key, ttl);

		helper.save(cache, value);

		assertAll(
			 () -> assertThat(helper.getTTL(key))
				  .isInstanceOf(Duration.class)
				  .isEqualTo(ttl),
			 () -> {
				 String actual = helper.find(key);
				 assertThat(actual).isEqualTo(value);
			 }
		);
	}

	@Test
	void find() {
		var key = "hello";
		var value = "redis";

		helper.save(newCache(key), value);

		assertThat(helper.<String>find(key)).isEqualTo(value);
	}

	@Test
	@DisplayName("조회할 Key 가 없다면 예외 발생")
	void getOrElseThrow() {
		var key = LocalDateTime.now() + UUID.randomUUID().toString();

		assertThatExceptionOfType(NotFoundDataException.class)
			 .isThrownBy(() -> helper.getOrElseThrow(key));
	}

	@Disabled("Embedded Server GETEX command 미지원")
	@DisplayName("TTL 설정 무효화"
		 + " 만료되지 않는 key 로 설정 후 조회")
	@Test
	void getAndChangedByEternalKey() {
		var cache = new DefaultCache("hello", Duration.ofSeconds(10));
		helper.save(cache, "redis");

		helper.getAndChangedByEternalKey(cache.key());

		assertThat(helper.getTTL(cache.key()).getSeconds())
			 .isEqualTo(-1);
	}

	@DisplayName("TTL 설정 변경")
	@Test
	void changeTTL() {
		String key = "hello";
		helper.save(newCache(key), "redis");

		var ttl = Duration.ofSeconds(10);
		helper.changeTTL(key, ttl);

		assertThat(ttl)
			 .isEqualTo(helper.getTTL(key));
	}

	@Test
	void evict() {
		var cache = newCache("TEST");
		helper.save(cache, UUID.randomUUID().toString());

		assertAll(
			 () -> assertThat(helper.evict(cache.key())).isTrue(),
			 () -> assertThat(helper.evict(cache.key())).isFalse()
		);
	}

	@DisplayName("정규식으로 등록된 캐시 키 조회")
	@Test
	void findKeys() {
		var key = "gmoon";
		helper.save(newCache(key + "1"), "value");
		helper.save(newCache(key + "2"), "value");

		var pattern = key + "*";
		assertThat(helper.findKeys(pattern))
			 .hasSize(2);
	}

	private Cache newCache(String key) {
		return new DefaultCache(key, CachePolicy.DEFAULT_TTL);
	}

	@DisplayName("RedisTemplate Data Types")
	@Nested
	class RedisTemplateTest {

		@Nested
		@DisplayName("Set data type "
			 + "smembers {key}")
		class SetType {
			private final SetOperations<String, Object> operations = helper.opsForSet();
			private final String key = "set-type";

			@BeforeEach
			void clear() {
				helper.evictAll();
			}

			@Test
			void add() {
				operations.add(key, "hello");
				operations.add(key, "redis");

				assertThat(operations.members(key))
					 .contains("redis", "hello");
			}
		}

		@DisplayName("List data type lrange {key} 0 -1")
		@Nested
		class ListType {
			private final ListOperations<String, Object> operations = helper.opsForList();
			private final String key = "list-type";

			@BeforeEach
			void clear() {
				helper.evictAll();
			}

			@Test
			@DisplayName("오른쪽 위치에 저장")
			void rightPush() {
				operations.rightPush(key, "r");
				operations.rightPush(key, "e");
				operations.rightPush(key, "d");
				operations.rightPush(key, "i");
				operations.rightPush(key, "s");

				assertThat(getAllElements()).containsExactly("r", "e", "d", "i", "s");
			}

			@Test
			void rightPushAll() {
				operations.rightPushAll(key, List.of("r", "e", "d", "i", "s"));

				assertThat(getAllElements()).containsExactly("r", "e", "d", "i", "s");
			}

			@Test
			void leftPushAll() {
				operations.leftPushAll(key, List.of("r", "e", "d", "i", "s"));

				operations.leftPop(key); // 왼쪽 요소 제거
				assertThat(getAllElements()).containsExactly("i", "d", "e", "r");
			}

			@DisplayName("키의 지정된 위치에 있는 요소의 값 반환")
			@Test
			void index() {
				operations.rightPushAll(key, List.of("r", "e", "d", "i", "s"));

				assertAll(
					 () -> assertThat(operations.index(key, 0)).isEqualTo("r"),
					 () -> assertThat(operations.index(key, 1)).isEqualTo("e"),
					 () -> assertThat(operations.index(key, 2)).isEqualTo("d"),
					 () -> assertThat(operations.index(key, 3)).isEqualTo("i"),
					 () -> assertThat(operations.index(key, 4)).isEqualTo("s"),

					 () -> assertThat(operations.index(key, -1)).isEqualTo("s"),
					 () -> assertThat(operations.index(key, 5)).isNull()
				);
			}

			@DisplayName("지정된 위치의 요소들만 새롭게 가공")
			@Test
			void trim() {
				operations.rightPushAll(key, List.of("r", "e", "d", "i", "s"));

				operations.trim(key, 1, 3);

				assertThat(getAllElements()).containsExactly("e", "d", "i");
			}

			private List<Object> getAllElements() {
				return operations.range(key, 0, -1);
			}
		}

		@DisplayName("Hash data type hgetall {key} hget {key} {filed}")
		@Nested
		class HashType {
			private final String key = "hash-type";

			@BeforeEach
			void clear() {
				helper.evictAll();
			}

			@DisplayName("해시 키 설정")
			@Test
			void put() {
				HashOperations<String, String, String> hashOperations = helper.opsForHash();

				var group1 = List.of("gmoon", "lee", "park");
				var group2 = List.of("anonymous");

				hashOperations.put(key, "group1", JacksonUtils.toString(group1));
				hashOperations.put(key, "group2", JacksonUtils.toString(group2));

				var groupJsonString1 = hashOperations.get(key, "group1");
				var groupJsonString2 = hashOperations.get(key, "group2");

				assertAll(
					 () -> assertThat(JacksonUtils.toObject(groupJsonString1, List.class))
						  .hasSize(3)
						  .containsAll(group1),
					 () -> assertThat(JacksonUtils.toObject(groupJsonString2, List.class))
						  .hasSize(1)
						  .containsAll(group2)
				);
			}

			@DisplayName("키에 저장된 해시 키 삭제")
			@Test
			void delete() {
				HashOperations<String, String, String> hashOperations = helper.opsForHash();

				var group1 = List.of("gmoon", "lee", "park");
				var group2 = List.of("anonymous");

				hashOperations.put(key, "group1", JacksonUtils.toString(group1));
				hashOperations.put(key, "group2", JacksonUtils.toString(group2));

				var hashKeys = hashOperations.keys(key);
				hashOperations.delete(key, hashKeys.toArray());

				assertAll(
					 () -> assertThat(hashOperations.hasKey(key, "group1")).isFalse(),
					 () -> assertThat(hashOperations.hasKey(key, "group2")).isFalse()
				);
			}

			@DisplayName("지정된 키의 엔트리 조회")
			@Test
			void entries() {
				HashOperations<String, String, List<String>> hashOperations = helper.opsForHash();

				var group1 = List.of("gmoon", "lee", "park");
				var group2 = List.of("anonymous");

				hashOperations.putAll(key, Map.of("group1", group1, "group2", group2));

				assertThat(hashOperations.entries(key))
					 .hasSize(2)
					 .contains(entry("group1", group1), entry("group2", group2));
			}

			@DisplayName("전달된 값을 기반으로 해시 키의 키를 누적하는 데 사용, "
				 + "전달된 값은 double 또는 long만 될 수 있으며 부동 소수점은 허용하지 않는다.")
			@Test
			void increment() {
				HashOperations<String, String, Integer> hashOperations = helper.opsForHash();

				var plus = 2;

				var seq = hashOperations.increment(key, "seq", plus);

				assertThat(seq).isEqualTo(plus);
			}

			@DisplayName("hash key 로 데이터를 한번에 가져온다.")
			@Test
			void multiGet() {
				HashOperations<String, String, List<String>> hashOperations = helper.opsForHash();

				hashOperations.putAll(key, Map.of(
					 "user", List.of("gmoon", "lee", "park"),
					 "guest", List.of("anonymous")
				));

				List<String> keys = List.of("user", "guest");
				List<List<String>> actual = hashOperations.multiGet(key, keys);

				assertThat(actual.stream().flatMap(Collection::stream))
					 .containsExactly("gmoon", "lee", "park", "anonymous");
			}

			@DisplayName("hash key 로 데이터를 한번에 가져온다. MultiGet 일급 컬렉션")
			@Test
			void multiGetWithFirstClass() {
				HashOperations<String, String, List<String>> hashOperations = helper.opsForHash();
				var groups = Map.of(
					 "group1", List.of("gmoon", "lee", "park"),
					 "group2", List.of("anonymous")
				);
				hashOperations.putAll(key, groups);

				var keys = List.of("group1", "group2");
				List<String> actual = hashOperations.multiGet(key, keys).stream()
					 .flatMap(Collection::stream)
					 .toList();

				assertThat(actual)
					 .contains("gmoon", "lee", "park", "anonymous");
			}
		}
	}
}
