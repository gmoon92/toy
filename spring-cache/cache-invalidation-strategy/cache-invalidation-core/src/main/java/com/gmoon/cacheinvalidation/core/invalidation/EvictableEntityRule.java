package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.gmoon.cacheinvalidation.core.policy.CachePolicy;

public class EvictableEntityRule implements InvalidationRule {

	private final Set<CachePolicy> owned;

	private EvictableEntityRule(Set<CachePolicy> owned) {
		this.owned = owned;
	}

	public static EvictableEntityRule owning(CachePolicy... policies) {
		return new EvictableEntityRule(Set.copyOf(Arrays.asList(policies)));
	}

	@Override
	public boolean supports(EntityChange change) {
		return change.isTypeOf(CacheEvictable.class);
	}

	@Override
	public Collection<CacheKey> resolve(EntityChange change) {
		return change.entity() instanceof CacheEvictable target
			 ? target.cacheEntriesToEvict()
			 : List.of();
	}

	@Override
	public Set<CachePolicy> owns() {
		return owned;
	}
}
