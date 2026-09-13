package com.gmoon.cacheinvalidation.core.invalidation;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;
import com.gmoon.cacheinvalidation.core.policy.CachePolicy;

@DisplayName("무효화 소유권 대조")
class InvalidationOwnershipTest {

	private static final List<CachePolicy> DECLARED =
		 List.of(TestCachePolicy.USER, TestCachePolicy.USER_SUMMARY);

	@Nested
	@DisplayName("선언한 캐시를 아무 규칙도 소유하지 않으면")
	class WhenPolicyHasNoOwner {

		@Test
		@DisplayName("이름을 밝히며 거부한다")
		void rejectsNamingTheCache() {
			InvalidationOwnership ownership = InvalidationOwnership.of(
				 List.of(EvictableEntityRule.owning(TestCachePolicy.USER)));

			assertThatIllegalStateException()
				 .as("주인 없는 캐시를 허용하면 무효화 누락이 운영 중에만 드러난다")
				 .isThrownBy(() -> ownership.verifyCovers(DECLARED))
				 .withMessageContaining(TestCachePolicy.Name.USER_SUMMARY);
		}
	}

	@Nested
	@DisplayName("TTL 에 맡긴다고 선언한 캐시는")
	class WhenCoveredByTtlOnlyRule {

		@Test
		@DisplayName("지우는 규칙이 없어도 통과한다")
		void passesWithoutEvictingRule() {
			InvalidationOwnership ownership = InvalidationOwnership.of(List.of(
				 EvictableEntityRule.owning(TestCachePolicy.USER),
				 TtlOnlyRule.covering(TestCachePolicy.USER_SUMMARY)));

			assertThatNoException()
				 .as("시간 만료로 충분하다는 판단은 유효한 선택이고, 선언되면 실수와 구분된다")
				 .isThrownBy(() -> ownership.verifyCovers(DECLARED));
		}
	}

	@Nested
	@DisplayName("모듈이 선언하지 않은 캐시를 소유하겠다고 하면")
	class WhenRuleOwnsUndeclaredPolicy {

		@Test
		@DisplayName("거부한다")
		void rejects() {
			InvalidationOwnership ownership = InvalidationOwnership.of(
				 List.of(EvictableEntityRule.owning(TestCachePolicy.USER, TestCachePolicy.USER_SUMMARY)));

			assertThatIllegalStateException()
				 .as("남의 캐시를 소유 주장하면 두 모듈이 같은 키를 두고 엇갈린다")
				 .isThrownBy(() -> ownership.verifyCovers(List.of(TestCachePolicy.USER)))
				 .withMessageContaining(TestCachePolicy.Name.USER_SUMMARY);
		}
	}

	@Nested
	@DisplayName("선언한 캐시가 모두 주인을 가지면")
	class WhenEveryPolicyOwned {

		@Test
		@DisplayName("통과한다")
		void passes() {
			InvalidationOwnership ownership = InvalidationOwnership.of(List.of(
				 EvictableEntityRule.owning(TestCachePolicy.USER, TestCachePolicy.USER_SUMMARY)));

			assertThatNoException().isThrownBy(() -> ownership.verifyCovers(DECLARED));
		}
	}
}
