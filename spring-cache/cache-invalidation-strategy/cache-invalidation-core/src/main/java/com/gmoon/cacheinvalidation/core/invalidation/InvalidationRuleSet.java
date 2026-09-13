package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.gmoon.cacheinvalidation.core.invalidation.change.EntityChange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class InvalidationRuleSet {

	private final List<InvalidationRule> rules;
	private final InvalidationRecorder recorder;

	public Set<CacheKey> resolve(EntityChange change) {
		return rules.stream()
			 .map(rule -> resolveInIsolation(rule, change))
			 .flatMap(Collection::stream)
			 .collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private Collection<CacheKey> resolveInIsolation(InvalidationRule rule, EntityChange change) {
		try {
			return rule.supports(change) ? rule.resolve(change) : List.of();
		} catch (RuntimeException e) {
			recorder.recordRuleFailure(rule.getClass().getSimpleName());
			log.warn("cache invalidation rule failed. rule: {}", rule.getClass().getSimpleName(), e);
			return List.of();
		}
	}
}
