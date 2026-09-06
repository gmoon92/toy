package com.gmoon.cacheinvalidation.core.cache;

import java.util.Collection;

@FunctionalInterface
public interface CachePolicies {

	Collection<CachePolicy> all();

	static CachePolicies of(CachePolicy... policies) {
		return () -> java.util.List.of(policies);
	}
}
