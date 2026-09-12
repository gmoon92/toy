package com.gmoon.cacheinvalidation.core.invalidation;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.gmoon.cacheinvalidation.core.cache.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;
import com.gmoon.cacheinvalidation.core.resilience.InvalidationRecorder;

@DisplayName("무효화 규칙 레지스트리")
class CacheInvalidationRulesTest {

	private final EntityChange change = EntityChange.inserted(new Object(), 1L);
	private final InvalidationRecorder recorder = new InvalidationRecorder();

	@Nested
	@DisplayName("변경 하나에 여러 규칙이 반응하면")
	class WhenMultipleRulesMatch {

		private final CacheInvalidationRules rules = new CacheInvalidationRules(List.of(
			 ruleOf(true, CacheEntryRef.of(TestCachePolicy.USER, 1L)),
			 ruleOf(true, CacheEntryRef.of(TestCachePolicy.USER_SUMMARY, 1L))
		), recorder);

		@Test
		@DisplayName("모든 규칙의 대상을 합쳐 반환한다")
		void mergesEntriesFromEveryMatchedRule() {
			assertThat(rules.resolve(change))
				 .as("교차 엔티티 규칙이 추가되어도 기존 규칙과 함께 동작해야 한다")
				 .containsExactlyInAnyOrder(
					  CacheEntryRef.of(TestCachePolicy.USER, 1L),
					  CacheEntryRef.of(TestCachePolicy.USER_SUMMARY, 1L));
		}
	}

	@Nested
	@DisplayName("서로 다른 규칙이 같은 대상을 지목하면")
	class WhenRulesOverlap {

		private final CacheInvalidationRules rules = new CacheInvalidationRules(List.of(
			 ruleOf(true, CacheEntryRef.of(TestCachePolicy.USER, 1L)),
			 ruleOf(true, CacheEntryRef.of(TestCachePolicy.USER, 1L))
		), recorder);

		@Test
		@DisplayName("중복을 제거해 한 번만 무효화한다")
		void deduplicatesEntries() {
			assertThat(rules.resolve(change)).hasSize(1);
		}
	}

	@Nested
	@DisplayName("반응하는 규칙이 없으면")
	class WhenNoRuleMatches {

		private final CacheInvalidationRules rules = new CacheInvalidationRules(List.of(
			 ruleOf(false, CacheEntryRef.of(TestCachePolicy.USER, 1L))
		), recorder);

		@Test
		@DisplayName("무효화 대상이 비어 있다")
		void resolvesNothing() {
			assertThat(rules.resolve(change)).isEmpty();
		}
	}

	@Nested
	@DisplayName("규칙 하나가 예외를 던지면")
	class WhenRuleThrows {

		private final CacheInvalidationRules rules = new CacheInvalidationRules(List.of(
			 failingRule(),
			 ruleOf(true, CacheEntryRef.of(TestCachePolicy.USER, 1L))
		), recorder);

		@Test
		@DisplayName("나머지 규칙은 계속 수행된다")
		void keepsResolvingRemainingRules() {
			assertThat(rules.resolve(change))
				 .as("규칙 하나의 실패가 다른 캐시의 무효화를 막으면 stale이 남는다")
				 .containsExactly(CacheEntryRef.of(TestCachePolicy.USER, 1L));
		}
	}

	private static CacheInvalidationRule ruleOf(boolean supported, CacheEntryRef entry) {
		return new CacheInvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return supported;
			}

			@Override
			public Collection<CacheEntryRef> resolve(EntityChange change) {
				return List.of(entry);
			}
		};
	}

	private static CacheInvalidationRule failingRule() {
		return new CacheInvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return true;
			}

			@Override
			public Collection<CacheEntryRef> resolve(EntityChange change) {
				throw new IllegalStateException("rule failed");
			}
		};
	}
}
