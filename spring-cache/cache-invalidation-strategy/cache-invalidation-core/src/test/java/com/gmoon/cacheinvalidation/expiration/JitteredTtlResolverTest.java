package com.gmoon.cacheinvalidation.expiration;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.function.LongUnaryOperator;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.support.NullValue;

@DisplayName("TTL 만료 해석")
class JitteredTtlResolverTest {

	private static final Duration BASE_TTL = Duration.ofMinutes(10);
	private static final Duration NOT_FOUND_TTL = Duration.ofSeconds(30);
	private static final double JITTER_RATIO = 0.1;

	@Nested
	@DisplayName("흔들림 폭은")
	class JitterBound {

		@Test
		@DisplayName("기준 TTL의 비율만큼이다")
		void isRatioOfDeclaredTtl() {
			assertThat(ttlOf(resolverWithOffset(bound -> bound), BASE_TTL))
				 .as("흔들림이 최대일 때의 상한")
				 .isEqualTo(Duration.ofMinutes(11));
			assertThat(ttlOf(resolverWithOffset(bound -> -bound), BASE_TTL))
				 .as("흔들림이 최소일 때의 하한")
				 .isEqualTo(Duration.ofMinutes(9));
		}

		@Test
		@DisplayName("TTL이 짧아도 최소 1초는 보장한다")
		void keepsMinimumWhenTtlIsShort() {
			assertThat(ttlOf(resolverWithOffset(bound -> bound), Duration.ofSeconds(1)))
				 .as("비율만 쓰면 짧은 TTL 은 흔들림이 0 이 되어 동시 만료를 막지 못한다")
				 .isEqualTo(Duration.ofSeconds(2));
		}
	}

	@Nested
	@DisplayName("흔들림이 기준 TTL 을 깎아내려도")
	class WhenJitterWouldExhaustTtl {

		@Test
		@DisplayName("만료 시간이 0 이하로 내려가지 않는다")
		void neverFallsToZeroOrBelow() {
			assertThat(ttlOf(resolverWithOffset(bound -> -bound), Duration.ofSeconds(1)))
				 .as("Spring Data Redis 는 0 이하 TTL 을 만료 없음으로 해석해 키가 영구히 남는다")
				 .isEqualTo(Duration.ofSeconds(1));
		}

		@Test
		@DisplayName("어떤 난수가 뽑혀도 지켜진다")
		void keepsMinimumForEveryDraw() {
			JitteredTtlResolver resolver = new JitteredTtlResolver(JITTER_RATIO, NOT_FOUND_TTL);

			assertThat(IntStream.range(0, 500)
				 .mapToObj(attempt -> ttlOf(resolver, Duration.ofSeconds(1)))
				 .toList())
				 .as("한 번만 새어도 그 키는 축출 전까지 영원히 stale 로 남는다")
				 .allMatch(ttl -> !ttl.isZero() && !ttl.isNegative());
		}
	}

	@Nested
	@DisplayName("흔들림 비율이 1 이상이면")
	class WhenJitterRatioTooLarge {

		@Test
		@DisplayName("기동에서 거부한다")
		void rejectsAtStartup() {
			assertThatIllegalArgumentException()
				 .as("기준 TTL 전체만큼 흔들면 선언한 수명이 의미를 잃는다")
				 .isThrownBy(() -> new JitteredTtlResolver(1.0, NOT_FOUND_TTL))
				 .withMessageContaining("jitter ratio");
		}
	}

	@Nested
	@DisplayName("같은 TTL을 여러 번 해석하면")
	class WhenResolvedRepeatedly {

		@Test
		@DisplayName("서로 다른 만료 시각이 나온다")
		void producesDifferentExpiries() {
			JitteredTtlResolver resolver = new JitteredTtlResolver(JITTER_RATIO, NOT_FOUND_TTL);

			long distinct = IntStream.range(0, 200)
				 .mapToObj(attempt -> ttlOf(resolver, BASE_TTL))
				 .distinct()
				 .count();

			assertThat(distinct)
				 .as("모두 같은 값이면 한꺼번에 만료되어 DB 로 몰린다")
				 .isGreaterThan(1);
		}
	}

	@Nested
	@DisplayName("값이 비어 있으면")
	class WhenValueIsAbsent {

		@Test
		@DisplayName("선언된 TTL 대신 not-found TTL 로 만료시킨다")
		void usesNotFoundTtl() {
			JitteredTtlResolver resolver = new JitteredTtlResolver(JITTER_RATIO, NOT_FOUND_TTL, bound -> 0);

			assertThat(resolver.functionFor(BASE_TTL).getTimeToLive("key", NullValue.INSTANCE))
				 .as("없는 키를 오래 붙들면 penetration 방어가 과해진다")
				 .isEqualTo(NOT_FOUND_TTL);
		}
	}

	private JitteredTtlResolver resolverWithOffset(LongUnaryOperator offsetGenerator) {
		return new JitteredTtlResolver(JITTER_RATIO, NOT_FOUND_TTL, offsetGenerator);
	}

	private Duration ttlOf(JitteredTtlResolver resolver, Duration declaredTtl) {
		return resolver.functionFor(declaredTtl).getTimeToLive("key", "value");
	}
}
