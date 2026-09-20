package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("무효화 소유권 대조")
class InvalidationRuleTest {

	private static final List<CachePolicy> DECLARED =
		 List.of(TestCachePolicy.USER, TestCachePolicy.USER_SUMMARY);

	@Nested
	@DisplayName("선언한 캐시를 아무 규칙도 소유하지 않으면")
	class WhenPolicyHasNoOwner {

		@Test
		@DisplayName("이름을 밝히며 거부한다")
		void rejectsNamingTheCache() {
			List<InvalidationRule> rules = List.of(owningRule(TestCachePolicy.USER));

			assertThatIllegalStateException()
				 .as("주인 없는 캐시를 허용하면 무효화 누락이 운영 중에만 드러난다")
				 .isThrownBy(() -> InvalidationRule.verifyOwnership(rules, DECLARED))
				 .withMessageContaining(TestCachePolicy.Name.USER_SUMMARY);
		}
	}

	@Nested
	@DisplayName("TTL 에 맡긴다고 선언한 캐시는")
	class WhenCoveredByTtlOnlyRule {

		@Test
		@DisplayName("지우는 규칙이 없어도 통과한다")
		void passesWithoutEvictingRule() {
			List<InvalidationRule> rules = List.of(
				 owningRule(TestCachePolicy.USER),
				 TtlOnlyRule.covering(TestCachePolicy.USER_SUMMARY));

			assertThatNoException()
				 .as("시간 만료로 충분하다는 판단은 유효한 선택이고, 선언되면 실수와 구분된다")
				 .isThrownBy(() -> InvalidationRule.verifyOwnership(rules, DECLARED));
		}
	}

	@Nested
	@DisplayName("모듈이 선언하지 않은 캐시를 소유하겠다고 하면")
	class WhenRuleOwnsUndeclaredPolicy {

		@Test
		@DisplayName("거부한다")
		void rejects() {
			List<InvalidationRule> rules = List.of(owningRule(TestCachePolicy.USER, TestCachePolicy.USER_SUMMARY));

			assertThatIllegalStateException()
				 .as("남의 캐시를 소유 주장하면 두 모듈이 같은 키를 두고 엇갈린다")
				 .isThrownBy(() -> InvalidationRule.verifyOwnership(rules, List.of(TestCachePolicy.USER)))
				 .withMessageContaining(TestCachePolicy.Name.USER_SUMMARY);
		}
	}

	@Nested
	@DisplayName("선언한 캐시가 모두 주인을 가지면")
	class WhenEveryPolicyOwned {

		@Test
		@DisplayName("통과한다")
		void passes() {
			List<InvalidationRule> rules = List.of(
				 owningRule(TestCachePolicy.USER, TestCachePolicy.USER_SUMMARY));

			assertThatNoException().isThrownBy(() -> InvalidationRule.verifyOwnership(rules, DECLARED));
		}
	}

	private static InvalidationRule owningRule(CachePolicy... policies) {
		return new InvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return true;
			}

			@Override
			public Collection<CacheKey> resolve(EntityChange change) {
				return List.of();
			}

			@Override
			public Set<CachePolicy> owns() {
				return Set.of(policies);
			}
		};
	}
}
