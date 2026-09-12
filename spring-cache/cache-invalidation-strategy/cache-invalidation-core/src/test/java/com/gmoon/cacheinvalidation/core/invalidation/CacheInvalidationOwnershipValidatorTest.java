package com.gmoon.cacheinvalidation.core.invalidation;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.gmoon.cacheinvalidation.core.cache.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.CachePolicies;
import com.gmoon.cacheinvalidation.core.cache.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.cache.InvalidationMode;
import com.gmoon.cacheinvalidation.core.resilience.InvalidationRecorder;

@DisplayName("무효화 소유권 기동 검증")
class CacheInvalidationOwnershipValidatorTest {

	@Nested
	@DisplayName("RULE 로 선언한 캐시를 아무 규칙도 소유하지 않으면")
	class WhenRuleModeCacheHasNoOwner {

		@Test
		@DisplayName("기동을 중단한다")
		void failsFast() {
			CacheInvalidationOwnershipValidator validator = validatorOf(
				 policyOf("ORPHAN", InvalidationMode.RULE));

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
			CacheInvalidationOwnershipValidator validator = validatorOf(
				 policyOf("TTL_BOUND", InvalidationMode.TTL_ONLY));

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
			CachePolicy registered = policyOf("REGISTERED", InvalidationMode.TTL_ONLY);
			CacheInvalidationOwnershipValidator validator = new CacheInvalidationOwnershipValidator(
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
			CachePolicy owned = policyOf("OWNED", InvalidationMode.RULE);
			CacheInvalidationOwnershipValidator validator = new CacheInvalidationOwnershipValidator(
				 new CachePolicyRegistry(List.of(policiesOf(owned))),
				 rulesOf(ruleOwning("OWNED")));

			assertThatNoException().isThrownBy(validator::afterPropertiesSet);
		}
	}

	private CacheInvalidationOwnershipValidator validatorOf(CachePolicy policy) {
		return new CacheInvalidationOwnershipValidator(
			 new CachePolicyRegistry(List.of(policiesOf(policy))),
			 rulesOf());
	}

	private CacheInvalidationRules rulesOf(CacheInvalidationRule... rules) {
		return new CacheInvalidationRules(List.of(rules), new InvalidationRecorder());
	}

	private CachePolicies policiesOf(CachePolicy policy) {
		return () -> List.of(policy);
	}

	private CachePolicy policyOf(String cacheName, InvalidationMode mode) {
		return new CachePolicy() {
			@Override
			public String cacheName() {
				return cacheName;
			}

			@Override
			public Duration ttl() {
				return Duration.ofMinutes(1);
			}

			@Override
			public Class<?> valueType() {
				return String.class;
			}

			@Override
			public InvalidationMode invalidationMode() {
				return mode;
			}
		};
	}

	private CacheInvalidationRule ruleOwning(String cacheName) {
		return new CacheInvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return false;
			}

			@Override
			public java.util.Collection<CacheEntryRef> resolve(EntityChange change) {
				return List.of();
			}

			@Override
			public Set<String> ownedCacheNames() {
				return Set.of(cacheName);
			}
		};
	}
}
