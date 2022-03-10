package com.gmoon.springdataredis.redis;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.gmoon.springdataredis.exception.NotFoundDataException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTemplateUtils {
	private static final String ALL = "*";

	private final RedisTemplate<String, Object> template;

	public void save(Cache cache, Serializable value) {
		template.opsForValue()
			.set(cache.getKey(), value, cache.getTtl());
	}

	public <T extends Serializable> T find(String key) {
		return (T)template.opsForValue().get(key);
	}

	public <T extends Serializable> T getOrElseThrow(String key) {
		if (BooleanUtils.toBoolean(template.hasKey(key))) {
			return find(key);
		}

		throw new NotFoundDataException(key);
	}

	public <T extends Serializable> T getAndChangedByEternalKey(String key) {
		return (T)template.opsForValue().getAndPersist(key);
	}

	public void changeTTL(String key, Duration ttl) {
		template.expire(key, ttl);
	}

	public Long getTTL(String key) {
		return template.getExpire(key);
	}

	public boolean delete(String... key) {
		return delete(Arrays.asList(key));
	}

	// Redis PipeLining used
	public boolean delete(Collection<String> keys) {
		Long result = template.delete(keys);
		if (Objects.isNull(result)) {
			return false;
		}
		return BooleanUtils.toBoolean(result.intValue());
	}

	public boolean deleteAll() {
		Set<String> keys = keys(ALL);
		log.info("delete keys: {}", keys);
		return delete(keys);
	}

	public Set<String> keys(String pattern) {
		return template.keys(pattern);
	}
}
