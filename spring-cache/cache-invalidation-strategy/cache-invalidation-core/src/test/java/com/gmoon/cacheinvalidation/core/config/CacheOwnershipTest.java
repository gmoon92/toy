package com.gmoon.cacheinvalidation.core.config;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.policy.InvalidationOwner;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRule;
import com.gmoon.cacheinvalidation.core.invalidation.event.EntityChange;

/**
 * 무효화 주인이 없는 캐시를 기동에서 막는다.
 * 검증이 설정 안에 있으므로 컨텍스트 없이 설정 객체를 직접 만들어 확인한다.
 */
@DisplayName("무효화 소유권 기동 검증")
class CacheOwnershipTest {

	@Nested
	@DisplayName("RULE 로 선언한 캐시를 아무 규칙도 소유하지 않으면")
	class WhenRuleOwnedCacheHasNoRule {

		@Test
		@DisplayName("기동을 중단한다")
		void failsFast() {
			AbstractRedisCacheConfig config = configOf(policyOf("ORPHAN", InvalidationOwner.RULE));

			assertThatIllegalStateException()
				 .as("소유자 없는 캐시를 허용하면 무효화 누락이 운영 중에만 드러난다")
				 .isThrownBy(config::validateInvalidationOwnership)
				 .withMessageContaining("ORPHAN");
		}
	}

	@Nested
	@DisplayName("TTL_ONLY 로 선언한 캐시는")
	class WhenTtlOnlyCache {

		@Test
		@DisplayName("소유 규칙이 없어도 기동한다")
		void startsWithoutRule() {
			AbstractRedisCacheConfig config = configOf(policyOf("TTL_BOUND", InvalidationOwner.TTL_ONLY));

			assertThatNoException()
				 .as("TTL 만으로 최신성을 보장하겠다는 선언은 유효한 선택이다")
				 .isThrownBy(config::validateInvalidationOwnership);
		}
	}

	@Nested
	@DisplayName("규칙이 등록되지 않은 캐시명을 소유하겠다고 선언하면")
	class WhenRuleOwnsUnknownCache {

		@Test
		@DisplayName("기동을 중단한다")
		void failsFast() {
			AbstractRedisCacheConfig config = configOf(
				 List.of(policyOf("REGISTERED", InvalidationOwner.TTL_ONLY)),
				 ruleOwning("TYPO_CACHE"));

			assertThatIllegalStateException()
				 .as("캐시명 오타는 조용한 무효화 실패로 이어진다")
				 .isThrownBy(config::validateInvalidationOwnership)
				 .withMessageContaining("TYPO_CACHE");
		}
	}

	@Nested
	@DisplayName("RULE 캐시를 소유하는 규칙이 있으면")
	class WhenRuleOwnsCache {

		@Test
		@DisplayName("기동한다")
		void starts() {
			AbstractRedisCacheConfig config = configOf(
				 List.of(policyOf("OWNED", InvalidationOwner.RULE)),
				 ruleOwning("OWNED"));

			assertThatNoException().isThrownBy(config::validateInvalidationOwnership);
		}
	}

	private AbstractRedisCacheConfig configOf(CachePolicy policy) {
		return configOf(List.of(policy));
	}

	private AbstractRedisCacheConfig configOf(List<CachePolicy> policies, InvalidationRule... rules) {
		return new AbstractRedisCacheConfig() {
			@Override
			protected List<CachePolicy> cachePolicies() {
				return policies;
			}

			@Override
			protected List<InvalidationRule> invalidationRules() {
				return List.of(rules);
			}
		};
	}

	private CachePolicy policyOf(String cacheName, InvalidationOwner owner) {
		CachePolicy.Spec spec = CachePolicy.Spec.of(cacheName, Duration.ofMinutes(1), String.class);
		CachePolicy.Spec applied = owner == InvalidationOwner.TTL_ONLY ? spec.invalidatedByTtlOnly() : spec;
		return () -> applied;
	}

	private InvalidationRule ruleOwning(String cacheName) {
		return new InvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return false;
			}

			@Override
			public Collection<CacheEntryRef> resolve(EntityChange change) {
				return List.of();
			}

			@Override
			public Set<String> ownedCacheNames() {
				return Set.of(cacheName);
			}
		};
	}
}
