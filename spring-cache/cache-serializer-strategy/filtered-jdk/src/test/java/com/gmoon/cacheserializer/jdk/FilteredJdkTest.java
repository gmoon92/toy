package com.gmoon.cacheserializer.jdk;

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

import com.gmoon.cacheserializer.test.CachedItem;
import com.gmoon.cacheserializer.test.CachedOrder;
import com.gmoon.cacheserializer.test.Gadget;
import com.gmoon.cacheserializer.test.Parent;
import com.gmoon.cacheserializer.test.UnserializableOrder;

@DisplayName("역직렬화 필터를 건 JDK 직렬화")
class FilteredJdkTest {

	private static final ObjectMapper JSON = JsonMapper.builder()
		 .addModule(new JavaTimeModule())
		 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		 .build();

	private final RedisSerializer<Object> serializer = FilteredJdk.allowing(
		 "com.gmoon.cacheserializer.test.CachedOrder",
		 "com.gmoon.cacheserializer.test.CachedItem",
		 "com.gmoon.cacheserializer.test.Parent",
		 "com.gmoon.cacheserializer.test.Parent$Child",
		 "java.util.*",
		 "java.time.*",
		 "java.lang.*"
	);

	@Nested
	@DisplayName("허용 목록 밖의 클래스가 실린 값을 읽으면")
	class WhenValueCarriesForeignClass {

		@BeforeEach
		void resetTrace() {
			Gadget.EXECUTED.set(false);
		}

		@Test
		@DisplayName("필터가 없으면 읽는 것만으로 readObject 가 실행된다")
		void executesHookWithoutFilter() {
			byte[] payload = RedisSerializer.java().serialize(new Gadget());

			RedisSerializer.java().deserialize(payload);

			assertThat(Gadget.EXECUTED)
				 .as("Spring Boot 캐시의 기본 직렬화기는 필터가 없어 Redis 에 쓸 수 있으면 코드 실행 경로가 열린다")
				 .isTrue();
		}

		@Test
		@DisplayName("필터가 있으면 readObject 가 돌기 전에 거부한다")
		void rejectsBeforeHook() {
			byte[] payload = RedisSerializer.java().serialize(new Gadget());

			assertThatThrownBy(() -> serializer.deserialize(payload))
				 .isInstanceOf(SerializationException.class);
			assertThat(Gadget.EXECUTED)
				 .as("필터는 클래스 기술자를 읽는 시점에 판정하므로 훅이 실행되기 전에 멈춰야 한다")
				 .isFalse();
		}
	}

	@Nested
	@DisplayName("허용 목록에 패턴을 적지 않으면")
	class WhenNoPatternGiven {

		@Test
		@DisplayName("만들지 못한다")
		void rejectsEmptyAllowlist() {
			assertThatIllegalArgumentException()
				 .as("빈 목록에 기본 거부만 붙으면 어떤 값도 읽지 못하는 캐시가 조용히 생긴다")
				 .isThrownBy(FilteredJdk::allowing);
		}
	}

	@Nested
	@DisplayName("Serializable 을 구현한 record 면")
	class WhenRecordIsSerializable {

		@Test
		@DisplayName("같은 값으로 돌아온다")
		void roundTrips() {
			CachedOrder order = CachedOrder.sample();

			assertThat(serializer.deserialize(serializer.serialize(order))).isEqualTo(order);
		}
	}

	@Nested
	@DisplayName("Serializable 을 구현하지 않은 record 면")
	class WhenRecordIsNotSerializable {

		@Test
		@DisplayName("저장에 실패한다")
		void failsToWrite() {
			assertThatThrownBy(() -> serializer.serialize(new UnserializableOrder(1L)))
				 .as("값을 쓰는 순간 실패하므로 캐시를 처음 채우는 트래픽에서 드러난다")
				 .isInstanceOf(SerializationException.class);
		}
	}

	@Nested
	@DisplayName("양방향 참조를 담으면")
	class WhenGraphHasCycle {

		@Test
		@DisplayName("참조 관계까지 그대로 복원한다")
		void preservesCycle() {
			Parent read = (Parent)serializer.deserialize(serializer.serialize(Parent.withChild()));

			assertThat(read.getChildren().get(0).getParent())
				 .as("JDK 가 순환을 보존하기 때문에 엔티티 캐시가 지금까지 동작했다. JSON 으로 옮기면 이 보존이 사라진다")
				 .isSameAs(read);
		}
	}

	@Nested
	@DisplayName("값을 쓰면")
	class WhenWriting {

		@Test
		@DisplayName("필드 이름을 함께 남긴다")
		void writesFieldNames() {
			String written = new String(serializer.serialize(new CachedItem("A", 2)), StandardCharsets.ISO_8859_1);

			assertThat(written)
				 .as("필드를 이름으로 맞추므로 필드 이름을 바꾸면 JDK 에서도 그 값을 잃는다")
				 .contains("sku")
				 .contains("qty");
		}

		@Test
		@DisplayName("같은 값을 쓴 JSON 보다 크다")
		void isLargerThanJson() throws JsonProcessingException {
			CachedOrder order = CachedOrder.sample();
			int jdk = serializer.serialize(order).length;
			int json = JSON.writeValueAsBytes(order).length;

			assertThat(jdk)
				 .as("바이너리라서 작은 것이 아니다. 클래스 기술자를 값마다 싣는다. jdk=%d, json=%d", jdk, json)
				 .isGreaterThan(json);
		}
	}
}
