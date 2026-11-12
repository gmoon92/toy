package com.gmoon.ttlonly.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("캐시 정책 선언")
class CachePolicyTest {

	@Nested
	@DisplayName("선언된 모든 정책은")
	class EveryDeclaredPolicy {

		@Test
		@DisplayName("양수 TTL 을 가진다")
		void hasPositiveTtl() {
			assertThat(CachePolicy.values())
				 .as("Redis 는 0 이하 TTL 을 만료 없음으로 다뤄 stale 상한이 사라진다")
				 .allSatisfy(policy -> assertThat(policy.ttl()).isPositive());
		}
	}
}
