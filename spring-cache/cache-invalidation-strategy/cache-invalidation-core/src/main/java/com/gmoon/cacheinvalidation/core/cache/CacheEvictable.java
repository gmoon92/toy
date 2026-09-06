package com.gmoon.cacheinvalidation.core.cache;

import java.util.List;

public interface CacheEvictable {

	List<CacheEntryRef> cacheEntriesToEvict();
}
