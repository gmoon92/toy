package com.gmoon.hibernatesecondlevelcache.config;

import javax.cache.expiry.Duration;

import com.gmoon.hibernatesecondlevelcache.global.code.CommonCode;
import com.gmoon.hibernatesecondlevelcache.member.Member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CachePolicy {

	// jCache
	MEMBER(Member.class),
	COMMON_CODE(CommonCode.class),

	// Spring Cache
	MEMBER_ALL(CacheName.MEMBER_ALL),
	MEMBER_FIND_BY_ID(CacheName.MEMBER_FIND_BY_ID);

	private final String cacheName;
	private final Duration ttl;

	CachePolicy(String cacheName) {
		this(cacheName, Duration.ONE_MINUTE);
	}

	CachePolicy(Class<?> entity) {
		this(entity.getName(), Duration.ONE_MINUTE);
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class CacheName {

		public static final String MEMBER_ALL = "MEMBER_ALL";
		public static final String MEMBER_FIND_BY_ID = "MEMBER_FIND_BY_ID";
	}
}
