package com.gmoon.cacheinvalidation.core.cache.policy;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CachePolicyRegistry {

	private final Map<String, CachePolicy> policiesByCacheName;

	public CachePolicyRegistry(Collection<CachePolicy> policies) {
		this.policiesByCacheName = index(policies);
	}

	public Collection<CachePolicy> all() {
		return policiesByCacheName.values();
	}

	public Optional<CachePolicy> findByCacheName(String cacheName) {
		return Optional.ofNullable(policiesByCacheName.get(cacheName));
	}

	public CachePolicy getByCacheName(String cacheName) {
		return findByCacheName(cacheName)
			 .orElseThrow(() -> new IllegalArgumentException("Unregistered cache name: " + cacheName));
	}

	private Map<String, CachePolicy> index(Collection<CachePolicy> policies) {
		Map<String, CachePolicy> indexed = new LinkedHashMap<>();
		for (CachePolicy policy : policies) {
			rejectDuplicate(indexed, policy);
			indexed.put(policy.cacheName(), policy);
		}
		return Map.copyOf(indexed);
	}

	private void rejectDuplicate(Map<String, CachePolicy> indexed, CachePolicy policy) {
		CachePolicy registered = indexed.get(policy.cacheName());
		if (registered != null && registered != policy) {
			throw new IllegalStateException("Duplicated cache name: " + policy.cacheName());
		}
	}
}
