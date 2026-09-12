package com.gmoon.cacheinvalidation.core.cache;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface CachePolicies {

	Collection<CachePolicy> all();

	static CachePolicies of(CachePolicy... policies) {
		return () -> List.of(policies);
	}
}
