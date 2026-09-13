package com.gmoon.cacheinvalidation.core.policy;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CacheCatalog {

	private final Map<String, CachePolicy> policiesByCacheName;

	public CacheCatalog(Collection<CachePolicy> policies) {
		this.policiesByCacheName = index(policies);
	}

	public Collection<CachePolicy> all() {
		return policiesByCacheName.values();
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
