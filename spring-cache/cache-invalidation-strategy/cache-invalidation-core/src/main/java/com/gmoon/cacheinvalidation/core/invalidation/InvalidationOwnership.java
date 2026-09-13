package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.gmoon.cacheinvalidation.core.policy.CachePolicy;

/**
 * 규칙들이 밝힌 소유 캐시를 모아 모듈이 선언한 캐시 목록과 대조한다.
 */
public record InvalidationOwnership(Set<CachePolicy> ownedPolicies) {

	public InvalidationOwnership {
		ownedPolicies = Set.copyOf(ownedPolicies);
	}

	public static InvalidationOwnership of(Collection<InvalidationRule> rules) {
		return new InvalidationOwnership(rules.stream()
			 .map(InvalidationRule::owns)
			 .flatMap(Collection::stream)
			 .collect(Collectors.toUnmodifiableSet()));
	}

	public void verifyCovers(Collection<CachePolicy> declaredPolicies) {
		rejectPoliciesWithoutOwner(declaredPolicies);
		rejectOwnershipOfUndeclaredPolicies(declaredPolicies);
	}

	private void rejectPoliciesWithoutOwner(Collection<CachePolicy> declaredPolicies) {
		List<String> withoutOwner = declaredPolicies.stream()
			 .filter(policy -> !ownedPolicies.contains(policy))
			 .map(CachePolicy::cacheName)
			 .toList();

		if (!withoutOwner.isEmpty()) {
			throw new IllegalStateException(
				 "No InvalidationRule owns these caches: " + withoutOwner
					  + ". Declare an owning rule, or cover them with TtlOnlyRule.");
		}
	}

	private void rejectOwnershipOfUndeclaredPolicies(Collection<CachePolicy> declaredPolicies) {
		List<String> undeclared = ownedPolicies.stream()
			 .filter(policy -> !declaredPolicies.contains(policy))
			 .map(CachePolicy::cacheName)
			 .toList();

		if (!undeclared.isEmpty()) {
			throw new IllegalStateException(
				 "Rules claim ownership of caches this module does not declare: " + undeclared);
		}
	}
}
