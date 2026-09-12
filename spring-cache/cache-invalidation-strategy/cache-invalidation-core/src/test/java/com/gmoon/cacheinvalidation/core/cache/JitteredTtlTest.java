package com.gmoon.cacheinvalidation.core.cache;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TTL Jitter")
class JitteredTtlTest {

	private static final Duration BASE_TTL = Duration.ofMinutes(10);
	private static final double JITTER_RATIO = 0.1;

	@Nested
	@DisplayName("흔들림 폭은")
	class JitterBound {

		@Test
		@DisplayName("기준 TTL의 비율만큼이다")
		void isRatioOfBaseTtl() {
			JitteredTtl maxOffset = new JitteredTtl(JITTER_RATIO, bound -> bound);
			JitteredTtl minOffset = new JitteredTtl(JITTER_RATIO, bound -> -bound);

			assertThat(maxOffset.apply(BASE_TTL)).isEqualTo(Duration.ofMinutes(11));
			assertThat(minOffset.apply(BASE_TTL)).isEqualTo(Duration.ofMinutes(9));
		}

		@Test
		@DisplayName("TTL이 짧아도 최소 1초는 확보한다")
		void isAtLeastOneSecond() {
			JitteredTtl maxOffset = new JitteredTtl(JITTER_RATIO, bound -> bound);

			assertThat(maxOffset.apply(Duration.ofSeconds(5)))
				 .as("비율만 적용하면 짧은 TTL에서 흔들림이 0이 되어 동시 만료를 막지 못한다")
				 .isEqualTo(Duration.ofSeconds(6));
		}
	}

	@Nested
	@DisplayName("무작위 적용 시")
	class WhenRandomised {

		@Test
		@DisplayName("기준 TTL 주변 범위를 벗어나지 않는다")
		void staysWithinBound() {
			JitteredTtl jitteredTtl = new JitteredTtl(JITTER_RATIO);

			for (int attempt = 0; attempt < 1_000; attempt++) {
				assertThat(jitteredTtl.apply(BASE_TTL))
					 .isBetween(Duration.ofMinutes(9), Duration.ofMinutes(11));
			}
		}

		@Test
		@DisplayName("항상 같은 값이 나오지는 않는다")
		void producesVaryingValues() {
			JitteredTtl jitteredTtl = new JitteredTtl(JITTER_RATIO);

			long distinct = IntStream.range(0, 200)
				 .mapToObj(attempt -> jitteredTtl.apply(BASE_TTL))
				 .distinct()
				 .count();

			assertThat(distinct)
				 .as("모두 같은 값이면 대량 키가 동시에 만료되어 jitter가 무의미하다")
				 .isGreaterThan(1);
		}
	}
}
