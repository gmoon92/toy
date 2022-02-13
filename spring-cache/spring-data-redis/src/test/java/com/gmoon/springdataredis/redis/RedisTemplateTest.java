package com.gmoon.springdataredis.redis;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.context.TestConstructor;
import org.springframework.util.CollectionUtils;

import com.gmoon.javacore.util.JacksonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
public class RedisTemplateTest {
	final RedisTemplate<String, Object> template;

	@Nested
	@DisplayName("Set data type "
		+ "smembers {key}")
	class SetType {
		final SetOperations<String, Object> operations = template.opsForSet();
		final String key = "set-type";

		@BeforeEach
		void clearAll() {
			template.delete(key);
		}

		@Test
		void testAdd() {
			// when
			operations.add(key, "hello");
			operations.add(key, "redis");

			// then
			assertThat(operations.members(key))
				.containsExactly("redis", "hello");
		}
	}

	@Nested
	@DisplayName("List data type "
		+ "lrange {key} 0 -1")
	class ListType {
		final ListOperations operations = template.opsForList();
		final String key = "list-type";

		@BeforeEach
		void clearAll() {
			template.delete(key);
		}

		@Test
		@DisplayName("오른쪽 위치에 저장")
		void testRightPush() {
			// when
			operations.rightPush(key, "r");
			operations.rightPush(key, "e");
			operations.rightPush(key, "d");
			operations.rightPush(key, "i");
			operations.rightPush(key, "s");

			// then
			assertThat(getAllElements()).containsExactly("r", "e", "d", "i", "s");
		}

		@Test
		void testRightPushAll() {
			// when
			operations.rightPushAll(key, Arrays.asList("r", "e", "d", "i", "s"));

			// then
			assertThat(getAllElements()).containsExactly("r", "e", "d", "i", "s");
		}

		@Test
		void testLeftPushAll() {
			// when
			operations.leftPushAll(key, Arrays.asList("r", "e", "d", "i", "s"));

			// then
			operations.leftPop(key); // 왼쪽 요소 제거
			assertThat(getAllElements()).containsExactly("i", "d", "e", "r");
		}

		@Test
		@DisplayName("키의 지정된 위치에 있는 요소의 값 반환")
		void testIndex() {
			// given
			operations.rightPushAll(key, Arrays.asList("r", "e", "d", "i", "s"));

			// when then
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

		@Test
		@DisplayName("지정된 위치의 요소들만 새롭게 가공")
		void testTrim() {
			// given
			operations.rightPushAll(key, Arrays.asList("r", "e", "d", "i", "s"));

			// when
			operations.trim(key, 1, 3);

			// then
			assertThat(getAllElements()).containsExactly("e", "d", "i");
		}

		private List<String> getAllElements() {
			return operations.range(key, 0, -1);
		}
	}

	@Nested
	@DisplayName("Hash data type "
		+ "hgetall {key} "
		+ "hget {key} {filed}")
	class HashType {
		final HashOperations operations = template.opsForHash();
		final String key = "hash-type";

		@BeforeEach
		void clearAll() {
			Set<String> hashKeys = operations.keys(key);
			log.info("hashKeys: {}", hashKeys);
			if (!CollectionUtils.isEmpty(hashKeys)) {
				operations.delete(key, hashKeys.toArray());
			}
		}

		@Test
		@DisplayName("해시 키 설정")
		void testPut() {
			// given
			List<String> group1 = Arrays.asList("gmoon", "lee", "park");
			List<String> group2 = Arrays.asList("anonymous");

			// when
			operations.put(key, "group1", JacksonUtils.toString(group1));
			operations.put(key, "group2", JacksonUtils.toString(group2));

			// then
			String groupJsonString1 = (String)operations.get(key, "group1");
			String groupJsonString2 = (String)operations.get(key, "group2");

			assertAll(
				() -> assertThat(JacksonUtils.toObject(groupJsonString1, List.class))
					.hasSize(3)
					.containsAll(group1),
				() -> assertThat(JacksonUtils.toObject(groupJsonString2, List.class))
					.hasSize(1)
					.containsAll(group2)
			);
		}

		@Test
		@DisplayName("키에 저장된 해시 키 삭제")
		void testDelete() {
			// given
			List<String> group1 = Arrays.asList("gmoon", "lee", "park");
			List<String> group2 = Arrays.asList("anonymous");

			operations.put(key, "group1", JacksonUtils.toString(group1));
			operations.put(key, "group2", JacksonUtils.toString(group2));

			// when
			Set<String> hashKeys = operations.keys(key);
			operations.delete(key, hashKeys.toArray());

			// then
			assertAll(
				() -> assertThat(operations.hasKey(key, "group1")).isFalse(),
				() -> assertThat(operations.hasKey(key, "group2")).isFalse()
			);
		}

		@Test
		@DisplayName("지정된 키의 엔트리 조회")
		void testEntries() {
			// given
			List<String> group1 = Arrays.asList("gmoon", "lee", "park");
			List<String> group2 = Arrays.asList("anonymous");

			Map<String, List<String>> map = new HashMap<>();
			map.put("group1", group1);
			map.put("group2", group2);
			operations.putAll(key, map);

			// when
			Map<String, List<String>> entries = operations.entries(key);

			// then
			assertThat(entries)
				.hasSize(2)
				.contains(entry("group1", group1), entry("group2", group2));
		}

		@Test
		@DisplayName("전달된 값을 기반으로 해시 키의 키를 누적하는 데 사용, "
			+ "전달된 값은 double 또는 long만 될 수 있으며 부동 소수점은 허용하지 않는다.")
		void testIncrement() {
			// given
			int plus = 2;

			// when
			Long seq = operations.increment(key, "seq", plus);

			// then
			assertThat(seq).isEqualTo(plus);
		}

		@Test
		@DisplayName("hash key 로 데이터를 한번에 가져온다.")
		void testMultiGet() {
			// given
			List<String> names1 = Arrays.asList("gmoon", "lee", "park");
			List<String> names2 = Arrays.asList("anonymous");

			Map<String, List<String>> map = new HashMap<>();
			map.put("group1", names1);
			map.put("group2", names2);
			operations.putAll(key, map);

			// when
			List<List<String>> actual = operations.multiGet(key, Arrays.asList("group1", "group2"));

			// then
			assertThat(actual)
				.hasSize(2)
				.containsExactly(names1, names2);
		}

		@Test
		@DisplayName("hash key 로 데이터를 한번에 가져온다. "
			+ "MultiGet 일급 컬렉션")
		void testMultiGet_with_firstClass() {
			// given
			Names names1 = Names.from("gmoon", "lee", "park");
			Names names2 = Names.from("anonymous");

			Map<String, Names> map = new HashMap<>();
			map.put("group1", names1);
			map.put("group2", names2);
			operations.putAll(key, map);

			// when
			List<Names> actual = operations.multiGet(key, Arrays.asList("group1", "group2"));

			// then
			assertThat(actual)
				.hasSize(2)
				.containsExactly(names1, names2);
		}
	}
}
