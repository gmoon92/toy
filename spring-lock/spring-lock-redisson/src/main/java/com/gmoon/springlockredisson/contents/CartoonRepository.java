package com.gmoon.springlockredisson.contents;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class CartoonRepository {

	private static final Map<Long, Cartoon> CACHE;

	static {
		CACHE = new HashMap<>();
		CACHE.put(0L, new Cartoon(0L));
	}

	public Cartoon getById(Long id) {
		return CACHE.get(id);
	}

	public Cartoon save(Cartoon request) {
		try {
			Thread.sleep(Duration.ofMillis(60).toMillis());
			CACHE.put(request.getId(), request);
			return request;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
