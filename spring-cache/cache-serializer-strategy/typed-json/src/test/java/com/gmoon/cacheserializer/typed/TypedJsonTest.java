package com.gmoon.cacheserializer.typed;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.fasterxml.jackson.core.type.TypeReference;

import com.gmoon.cacheserializer.test.CachedOrder;
import com.gmoon.cacheserializer.test.LegacyOrder;
import com.gmoon.cacheserializer.test.Parent;
import com.gmoon.cacheserializer.test.UnserializableOrder;

@DisplayName("타입 고정 JSON")
class TypedJsonTest {

	private final RedisSerializer<CachedOrder> serializer = TypedJson.of(CachedOrder.class);

	@Nested
	@DisplayName("값을 쓰면")
	class WhenWriting {

		@Test
		@DisplayName("타입 정보 없이 데이터만 남긴다")
		void writesDataOnly() {
			String written = textOf(serializer.serialize(CachedOrder.sample()));

			assertThat(written)
				 .as("값에 클래스 이름이 없어야 값이 복원 타입을 고를 수 없다")
				 .doesNotContain("@class")
				 .doesNotContain(CachedOrder.class.getName());
		}

		@Test
		@DisplayName("읽으면 같은 값으로 돌아온다")
		void roundTrips() {
			CachedOrder order = CachedOrder.sample();

			assertThat(serializer.deserialize(serializer.serialize(order))).isEqualTo(order);
		}
	}

	@Nested
	@DisplayName("값을 쓴 클래스가 이동하거나 이름이 바뀌어도")
	class WhenWriterClassRenamed {

		@Test
		@DisplayName("읽는 쪽이 선언한 타입으로 읽는다")
		void readsWithDeclaredType() {
			byte[] writtenByOldClass = TypedJson.of(LegacyOrder.class).serialize(new LegacyOrder(1L, "PAID"));

			assertThat(TypedJson.of(UnserializableOrder.class).deserialize(writtenByOldClass))
				 .as("타입은 읽는 쪽 설정이 정하므로 쓴 클래스의 이름과 위치는 값에 남지 않는다")
				 .isEqualTo(new UnserializableOrder(1L));
		}
	}

	@Nested
	@DisplayName("값에 모르는 필드가 있으면")
	class WhenUnknownFieldPresent {

		@Test
		@DisplayName("무시하고 읽는다")
		void ignoresUnknownField() {
			byte[] writtenByNewerVersion = bytesOf("{\"id\":1,\"addedLater\":true}");

			assertThat(TypedJson.of(UnserializableOrder.class).deserialize(writtenByNewerVersion))
				 .as("롤링 배포 중 신버전이 필드를 추가해 쓴 값을 구버전이 읽지 못하면 배포가 곧 장애가 된다")
				 .isEqualTo(new UnserializableOrder(1L));
		}
	}

	@Nested
	@DisplayName("컬렉션을 원소 타입 없이 선언하면")
	class WhenCollectionDeclaredWithoutElementType {

		@Test
		@DisplayName("원소가 Map 으로 돌아와 꺼내 쓸 때 실패한다")
		void returnsMapsInsteadOfElements() {
			@SuppressWarnings("rawtypes")
			RedisSerializer<List> raw = TypedJson.of(List.class);
			List<?> read = raw.deserialize(raw.serialize(List.of(CachedOrder.sample())));

			assertThat(read.get(0))
				 .as("Spring Cache 는 반환 타입의 제네릭을 넘기지 않으므로 미스 때는 멀쩡하다가 적중 때 ClassCastException 이 난다")
				 .isInstanceOf(Map.class);
		}
	}

	@Nested
	@DisplayName("컬렉션을 원소 타입까지 선언하면")
	class WhenCollectionDeclaredWithElementType {

		@Test
		@DisplayName("원소 타입 그대로 돌아온다")
		void keepsElementType() {
			RedisSerializer<List<CachedOrder>> typed = TypedJson.of(new TypeReference<>() {
			});

			List<CachedOrder> read = typed.deserialize(typed.serialize(List.of(CachedOrder.sample())));

			assertThat(read.get(0)).isInstanceOf(CachedOrder.class);
		}
	}

	@Nested
	@DisplayName("양방향 참조를 담으면")
	class WhenGraphHasCycle {

		@Test
		@DisplayName("저장에 실패한다")
		void rejectsCycle() {
			RedisSerializer<Parent> cyclic = TypedJson.of(Parent.class);

			assertThatThrownBy(() -> cyclic.serialize(Parent.withChild()))
				 .as("엔티티를 그대로 담으면 양방향 연관이 무한 재귀가 된다. DTO 로 한 방향을 끊어야 한다")
				 .isInstanceOf(SerializationException.class);
		}
	}

	private static String textOf(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	private static byte[] bytesOf(String json) {
		return json.getBytes(StandardCharsets.UTF_8);
	}
}
