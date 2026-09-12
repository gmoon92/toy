package com.gmoon.cacheinvalidation.core.invalidation;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicies;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.cache.policy.InvalidationOwner;
import com.gmoon.cacheinvalidation.core.metrics.InvalidationRecorder;
import com.gmoon.cacheinvalidation.core.event.EntityChange;

@DisplayName("무효화 소유권 기동 검증")
class CacheOwnershipValidatorTest {

	@Nested
	@DisplayName("RULE 로 선언한 캐시를 아무 규칙도 소유하지 않으면")
	class WhenRuleModeCacheHasNoOwner {

		@Test
		@DisplayName("기동을 중단한다")
		void failsFast() {
			CacheOwnershipValidator validator = validatorOf(
				 policyOf("ORPHAN", InvalidationOwner.RULE));

			assertThatIllegalStateException()
				 .as("소유자 없는 캐시를 허용하면 무효화 누락이 운영 중에만 드러난다")
				 .isThrownBy(validator::afterPropertiesSet)
				 .withMessageContaining("ORPHAN");
		}
	}

	@Nested
	@DisplayName("TTL_ONLY 로 선언한 캐시는")
	class WhenTtlOnlyCache {

		@Test
		@DisplayName("소유 규칙이 없어도 기동한다")
		void startsWithoutOwner() {
			CacheOwnershipValidator validator = validatorOf(
				 policyOf("TTL_BOUND", InvalidationOwner.TTL_ONLY));

			assertThatNoException()
				 .as("TTL 만으로 최신성을 보장하겠다는 선언은 유효한 선택이다")
				 .isThrownBy(validator::afterPropertiesSet);
		}
	}

	@Nested
	@DisplayName("규칙이 등록되지 않은 캐시명을 소유하겠다고 선언하면")
	class WhenRuleOwnsUnregisteredCache {

		@Test
		@DisplayName("기동을 중단한다")
		void failsFast() {
			CachePolicy registered = policyOf("REGISTERED", InvalidationOwner.TTL_ONLY);
			CacheOwnershipValidator validator = new CacheOwnershipValidator(
				 new CachePolicyRegistry(List.of(policiesOf(registered))),
				 rulesOf(ruleOwning("TYPO_CACHE")));

			assertThatIllegalStateException()
				 .as("캐시명 오타는 조용한 무효화 실패로 이어진다")
				 .isThrownBy(validator::afterPropertiesSet)
				 .withMessageContaining("TYPO_CACHE");
		}
	}

	@Nested
	@DisplayName("RULE 캐시를 소유하는 규칙이 있으면")
	class WhenOwnerExists {

		@Test
		@DisplayName("기동한다")
		void starts() {
			CachePolicy owned = policyOf("OWNED", InvalidationOwner.RULE);
			CacheOwnershipValidator validator = new CacheOwnershipValidator(
				 new CachePolicyRegistry(List.of(policiesOf(owned))),
				 rulesOf(ruleOwning("OWNED")));

			assertThatNoException().isThrownBy(validator::afterPropertiesSet);
		}
	}

	private CacheOwnershipValidator validatorOf(CachePolicy policy) {
		return new CacheOwnershipValidator(
			 new CachePolicyRegistry(List.of(policiesOf(policy))),
			 rulesOf());
	}

	private InvalidationRules rulesOf(InvalidationRule... rules) {
		return new InvalidationRules(List.of(rules), new InvalidationRecorder());
	}

	private CachePolicies policiesOf(CachePolicy policy) {
		return () -> List.of(policy);
	}

	private CachePolicy policyOf(String cacheName, InvalidationOwner mode) {
		CachePolicy.Spec spec = CachePolicy.Spec.of(cacheName, Duration.ofMinutes(1), String.class);
		CachePolicy.Spec applied = mode == InvalidationOwner.TTL_ONLY ? spec.invalidatedByTtlOnly() : spec;
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
