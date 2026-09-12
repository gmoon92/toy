package com.gmoon.cacheinvalidation.core.cache.eviction;

import java.util.List;

public interface CacheEvictable {

	List<CacheEntryRef> cacheEntriesToEvict();
}
