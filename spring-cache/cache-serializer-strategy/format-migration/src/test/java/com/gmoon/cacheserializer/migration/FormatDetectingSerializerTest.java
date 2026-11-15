package com.gmoon.cacheserializer.migration;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.gmoon.cacheserializer.jdk.FilteredJdk;
import com.gmoon.cacheserializer.test.CachedOrder;
import com.gmoon.cacheserializer.test.LegacyOrder;
import com.gmoon.cacheserializer.typed.TypedJson;

@DisplayName("구 포맷을 함께 읽는 이관용 직렬화")
class FormatDetectingSerializerTest {

	private static final CachedOrder UPGRADED = new CachedOrder(1L, "PAID", List.of(), null);

	private final RedisSerializer<Object> legacy = FilteredJdk.allowing("com.gmoon.cacheserializer.test.LegacyOrder");
	private final FormatDetectingSerializer<CachedOrder> serializer = new FormatDetectingSerializer<>(
		 TypedJson.of(CachedOrder.class),
		 legacy,
		 FormatDetectingSerializerTest::upgrade
	);

	@Nested
	@DisplayName("구 포맷(JDK) 값을 읽으면")
	class WhenLegacyValueRead {

		@Test
		@DisplayName("구 값을 새 타입으로 바꿔 돌려준다")
		void upgradesLegacyValue() {
			byte[] legacyBytes = legacy.serialize(new LegacyOrder(1L, "PAID"));

			assertThat(serializer.deserialize(legacyBytes))
				 .as("이름을 바꿀 수 없는 캐시는 구 값이 TTL 로 사라질 때까지 둘 다 읽어야 한다")
				 .isEqualTo(UPGRADED);
		}

		@Test
		@DisplayName("구 포맷 적중을 센다")
		void countsLegacyRead() {
			serializer.deserialize(legacy.serialize(new LegacyOrder(1L, "PAID")));

			assertThat(serializer.legacyReads())
				 .as("이 수가 0 으로 내려와야 이관용 직렬화를 걷어낼 수 있다")
				 .isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("새 포맷(JSON) 값을 읽으면")
	class WhenCurrentValueRead {

		@Test
		@DisplayName("새 직렬화기로 읽고 구 포맷 적중으로 세지 않는다")
		void readsWithCurrentSerializer() {
			CachedOrder order = CachedOrder.sample();

			assertThat(serializer.deserialize(serializer.serialize(order))).isEqualTo(order);
			assertThat(serializer.legacyReads()).isZero();
		}
	}

	@Nested
	@DisplayName("값을 쓰면")
	class WhenWriting {

		@Test
		@DisplayName("새 포맷으로만 쓴다")
		void writesCurrentFormatOnly() {
			byte[] written = serializer.serialize(CachedOrder.sample());

			assertThat((char)written[0])
				 .as("구 포맷으로 다시 쓰면 구 값이 줄지 않아 이관이 끝나지 않는다")
				 .isEqualTo('{');
		}
	}

	@Nested
	@DisplayName("새 포맷도 구 포맷도 아닌 깨진 값이면")
	class WhenValueCorrupted {

		@Test
		@DisplayName("구 포맷으로 위장하지 않고 실패한다")
		void failsInsteadOfFallingBack() {
			byte[] corrupted = "{not-json".getBytes(StandardCharsets.UTF_8);

			assertThatThrownBy(() -> serializer.deserialize(corrupted))
				 .as("예외가 나면 구 포맷으로 다시 시도하는 분기는 진짜 고장을 정상 경로로 숨긴다")
				 .isInstanceOf(SerializationException.class);
			assertThat(serializer.legacyReads()).isZero();
		}
	}

	@Nested
	@DisplayName("빈 값이면")
	class WhenValueEmpty {

		@Test
		@DisplayName("null 을 돌려준다")
		void returnsNull() {
			assertThat(serializer.deserialize(new byte[0])).isNull();
			assertThat(serializer.deserialize(null)).isNull();
		}
	}

	private static CachedOrder upgrade(Object legacyValue) {
		LegacyOrder order = (LegacyOrder)legacyValue;
		return new CachedOrder(order.getId(), order.getStatus(), List.of(), null);
	}
}
