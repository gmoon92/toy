package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

import com.gmoon.cacheinvalidation.core.cache.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.cache.InvalidationMode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CacheInvalidationOwnershipValidator implements InitializingBean {

	private final CachePolicyRegistry registry;
	private final CacheInvalidationRules rules;

	@Override
	public void afterPropertiesSet() {
		Set<String> ownedCacheNames = rules.ownedCacheNames();
		rejectCachesWithoutOwner(ownedCacheNames);
		rejectOwnershipOfUnregisteredCaches(ownedCacheNames);
	}

	private void rejectCachesWithoutOwner(Set<String> ownedCacheNames) {
		List<String> withoutOwner = registry.all()
			 .stream()
			 .filter(policy -> policy.invalidationMode() == InvalidationMode.RULE)
			 .map(CachePolicy::cacheName)
			 .filter(cacheName -> !ownedCacheNames.contains(cacheName))
			 .toList();

		if (!withoutOwner.isEmpty()) {
			throw new IllegalStateException(
				 "No CacheInvalidationRule owns these caches: " + withoutOwner
					  + ". Declare an owning rule, or set invalidationMode() to TTL_ONLY.");
		}
	}

	private void rejectOwnershipOfUnregisteredCaches(Set<String> ownedCacheNames) {
		List<String> unregistered = ownedCacheNames.stream()
			 .filter(cacheName -> registry.findByCacheName(cacheName).isEmpty())
			 .toList();

		if (!unregistered.isEmpty()) {
			throw new IllegalStateException(
				 "Rules claim ownership of unregistered caches: " + unregistered);
		}
	}
}
