package com.gmoon.cacheserializer.polymorphic;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.gmoon.cacheserializer.test.CachedOrder;
import com.gmoon.outside.Intruder;

@DisplayName("타입 지명 JSON")
class PolymorphicJsonTest {

	private static final String OWN_PACKAGE = "com.gmoon.cacheserializer.";
	private static final ObjectMapper UNTYPED = JsonMapper.builder()
		 .addModule(new JavaTimeModule())
		 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		 .build();

	private final RedisSerializer<Object> serializer = PolymorphicJson.allowing(OWN_PACKAGE, "java.util.", "java.time.");

	@Nested
	@DisplayName("값을 쓰면")
	class WhenWriting {

		@Test
		@DisplayName("복원할 클래스 이름을 값 안에 남긴다")
		void writesClassName() {
			String written = textOf(serializer.serialize(CachedOrder.sample()));

			assertThat(written)
				 .as("타입을 몰라도 읽을 수 있는 대가로 값이 복원 타입을 지목한다")
				 .contains("\"@class\":\"" + CachedOrder.class.getName() + "\"");
		}

		@Test
		@DisplayName("선언 없이도 같은 값으로 돌아온다")
		void roundTripsWithoutDeclaration() {
			CachedOrder order = CachedOrder.sample();

			assertThat(serializer.deserialize(serializer.serialize(order))).isEqualTo(order);
		}

		@Test
		@DisplayName("타입 정보 없이 쓴 같은 값보다 크다")
		void isLargerThanUntypedJson() throws JsonProcessingException {
			CachedOrder order = CachedOrder.sample();
			int polymorphic = serializer.serialize(order).length;
			int untyped = UNTYPED.writeValueAsBytes(order).length;

			assertThat(polymorphic)
				 .as("중첩마다 클래스 이름이 반복된다. polymorphic=%d, untyped=%d", polymorphic, untyped)
				 .isGreaterThan(untyped);
		}
	}

	@Nested
	@DisplayName("값이 허용 목록 밖의 클래스를 지목하면")
	class WhenValueNamesForeignClass {

		@BeforeEach
		void resetTrace() {
			Intruder.CONSTRUCTED.set(false);
		}

		@Test
		@DisplayName("만들기 전에 거부한다")
		void rejectsBeforeConstruction() {
			byte[] forged = bytesOf("{\"@class\":\"" + Intruder.class.getName() + "\"}");

			assertThatThrownBy(() -> serializer.deserialize(forged))
				 .as("Redis 에 쓸 수 있는 공격자가 고른 클래스가 만들어지면 가젯 체인의 입구가 열린다")
				 .isInstanceOf(SerializationException.class);
			assertThat(Intruder.CONSTRUCTED)
				 .as("거부는 생성자가 돌기 전에 일어나야 한다")
				 .isFalse();
		}
	}

	@Nested
	@DisplayName("값을 쓴 클래스가 다른 패키지로 이동하면")
	class WhenWriterClassMoved {

		@Test
		@DisplayName("그 전에 쓴 값을 읽지 못한다")
		void failsToReadOldValues() {
			String written = textOf(serializer.serialize(CachedOrder.sample()));
			byte[] writtenBeforeMove = bytesOf(written.replace(CachedOrder.class.getName(), OWN_PACKAGE + "old.CachedOrder"));

			assertThatThrownBy(() -> serializer.deserialize(writtenBeforeMove))
				 .as("클래스 이름이 값에 박혀 있어 리팩토링 한 번이 해당 캐시 전체를 못 쓰게 만든다")
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
