package com.gmoon.springdataredis.user;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import com.gmoon.springdataredis.cache.Cache;
import com.gmoon.springdataredis.cache.CacheName;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RedisHash(value = CacheUser.KEY)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CacheUser implements Cache {
	static final String KEY = CacheName.Constants.USER;
	static final long MINUTES_OF_TTL = 5;

	@Id
	private String id;

	@Indexed
	@EqualsAndHashCode.Include
	private String username;
	private String email;
	private boolean enabled;

	@TimeToLive(unit = TimeUnit.MINUTES)
	private Long expiration;

	@Builder
	private CacheUser(String id, String username, String email, boolean enabled) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.enabled = enabled;
		this.expiration = MINUTES_OF_TTL;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public Duration getTtl() {
		return Duration.ofMinutes(MINUTES_OF_TTL);
	}
}
