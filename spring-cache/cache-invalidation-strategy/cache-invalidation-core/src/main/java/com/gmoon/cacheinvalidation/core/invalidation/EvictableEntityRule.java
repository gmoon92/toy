package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEvictable;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.invalidation.event.EntityChange;

public class EvictableEntityRule implements InvalidationRule {

	private final Set<String> ownedCacheNames;

	private EvictableEntityRule(Set<String> ownedCacheNames) {
		this.ownedCacheNames = ownedCacheNames;
	}

	public static EvictableEntityRule owning(CachePolicy... policies) {
		return new EvictableEntityRule(Arrays.stream(policies)
			 .map(CachePolicy::cacheName)
			 .collect(Collectors.toUnmodifiableSet()));
	}

	@Override
	public boolean supports(EntityChange change) {
		return change.isTypeOf(CacheEvictable.class);
	}

	@Override
	public Collection<CacheEntryRef> resolve(EntityChange change) {
		return change.entity() instanceof CacheEvictable target
			 ? target.cacheEntriesToEvict()
			 : List.of();
	}

	@Override
	public Set<String> ownedCacheNames() {
		return ownedCacheNames;
	}
}
