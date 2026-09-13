package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.gmoon.cacheinvalidation.core.invalidation.change.EntityChange;
import com.gmoon.cacheinvalidation.core.policy.CachePolicy;

/**
 * 지목한 캐시를 시간 만료에만 맡긴다고 선언한다.
 * <p>
 * 아무것도 지우지 않지만 주인은 있다. 무효화를 쓰는 모듈에서 빠진 캐시는
 * 실수인지 의도인지 구분되지 않으므로, 의도라면 이 규칙으로 남긴다.
 */
public class TtlOnlyRule implements InvalidationRule {

	private final Set<CachePolicy> owned;

	private TtlOnlyRule(Set<CachePolicy> owned) {
		this.owned = owned;
	}

	public static TtlOnlyRule covering(CachePolicy... policies) {
		return new TtlOnlyRule(Set.copyOf(Arrays.asList(policies)));
	}

	@Override
	public boolean supports(EntityChange change) {
		return false;
	}

	@Override
	public Collection<CacheKey> resolve(EntityChange change) {
		return List.of();
	}

	@Override
	public Set<CachePolicy> owns() {
		return owned;
	}
}
