package com.gmoon.springdataredis.helper;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import com.gmoon.javacore.util.BooleanUtils;
import com.gmoon.springdataredis.cache.Cache;
import com.gmoon.springdataredis.exception.NotFoundDataException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RedisTemplateHelper {

	private final RedisTemplate<String, Object> template;

	public void save(Cache cache, Object value) {
		template.opsForValue()
			 .set(cache.key(), value, cache.ttl());
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

	public Duration getTTL(String key) {
		return Duration.ofSeconds(template.getExpire(key));
	}

	public boolean evict(String... key) {
		return evict(Arrays.asList(key));
	}

	// Redis PipeLining used
	public boolean evict(Collection<String> keys) {
		Long result = template.delete(keys);
		if (Objects.isNull(result)) {
			return false;
		}
		return BooleanUtils.toBoolean(result.intValue());
	}

	public boolean evictAll() {
		String all = "*";
		Set<String> keys = findKeys(all);
		log.debug("evict keys: {}", keys);
		return evict(keys);
	}

	public Set<String> findKeys(String pattern) {
		return template.keys(pattern);
	}

	@SuppressWarnings({"all", "unchecked", "rawtypes"})
	public ListOperations opsForList() {
		return template.opsForList();
	}

	@SuppressWarnings({"all", "unchecked", "rawtypes"})
	public SetOperations opsForSet() {
		return template.opsForSet();
	}

	@SuppressWarnings({"all", "unchecked", "rawtypes"})
	public HashOperations opsForHash() {
		return template.opsForHash();
	}
}
