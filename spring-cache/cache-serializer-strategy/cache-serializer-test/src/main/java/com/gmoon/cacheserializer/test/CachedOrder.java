package com.gmoon.cacheserializer.test;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record CachedOrder(long id, String status, List<CachedItem> items, Instant placedAt) implements Serializable {

	public static CachedOrder sample() {
		List<CachedItem> items = List.of(new CachedItem("A", 2), new CachedItem("B", 1));
		return new CachedOrder(1L, "PAID", items, Instant.parse("2026-11-14T00:00:00Z"));
	}
}
