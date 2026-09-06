package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.gmoon.cacheinvalidation.core.cache.CacheEntryRef;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheInvalidationRules {

	private final List<CacheInvalidationRule> rules;

	public CacheInvalidationRules(List<CacheInvalidationRule> rules) {
		this.rules = List.copyOf(rules);
	}

	public Set<CacheEntryRef> resolve(EntityChange change) {
		return rules.stream()
			 .map(rule -> resolveQuietly(rule, change))
			 .flatMap(Collection::stream)
			 .collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private Collection<CacheEntryRef> resolveQuietly(CacheInvalidationRule rule, EntityChange change) {
		try {
			return rule.supports(change) ? rule.resolve(change) : List.of();
		} catch (RuntimeException e) {
			log.warn("cache invalidation rule failed. rule: {}", rule.getClass().getSimpleName(), e);
			return List.of();
		}
	}
}
