package com.gmoon.cacheinvalidation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 변경 하나가 어떤 캐시 항목을 못 쓰게 만드는지 결정한다.
 * <p>
 * {@link #owns()} 로 책임지는 캐시를 밝힌다. 무효화 방식이 TTL 뿐이어도
 * {@link TtlOnlyRule} 로 선언해야 주인 없는 캐시가 생기지 않는다.
 */
public interface InvalidationRule {

	/**
	 * 선언된 캐시마다 주인이 있는지, 규칙이 선언되지 않은 캐시를 주장하지 않는지 대조한다.
	 */
	static void verifyOwnership(Collection<InvalidationRule> rules, Collection<CachePolicy> declaredPolicies) {
		Set<CachePolicy> owned = rules.stream()
			 .map(InvalidationRule::owns)
			 .flatMap(Collection::stream)
			 .collect(Collectors.toUnmodifiableSet());

		List<String> withoutOwner = namesOf(declaredPolicies, policy -> !owned.contains(policy));
		if (!withoutOwner.isEmpty()) {
			throw new IllegalStateException("No InvalidationRule owns these caches: " + withoutOwner
				 + ". Declare an owning rule, or own them with TtlOnlyRule.owning(...).");
		}

		List<String> undeclared = namesOf(owned, policy -> !declaredPolicies.contains(policy));
		if (!undeclared.isEmpty()) {
			throw new IllegalStateException(
				 "Rules claim ownership of caches this module does not declare: " + undeclared);
		}
	}

	private static List<String> namesOf(Collection<CachePolicy> policies, Predicate<CachePolicy> filter) {
		return policies.stream().filter(filter).map(CachePolicy::cacheName).toList();
	}

	boolean supports(EntityChange change);

	Collection<CacheEntry> resolve(EntityChange change);

	default Set<CachePolicy> owns() {
		return Set.of();
	}
}
