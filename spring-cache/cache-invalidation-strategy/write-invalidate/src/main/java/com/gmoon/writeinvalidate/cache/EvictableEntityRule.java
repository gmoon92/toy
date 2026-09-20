package com.gmoon.writeinvalidate.cache;

import com.gmoon.cacheinvalidation.CacheKey;
import com.gmoon.cacheinvalidation.CachePolicy;
import com.gmoon.cacheinvalidation.EntityChange;
import com.gmoon.cacheinvalidation.InvalidationRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
