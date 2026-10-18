package com.gmoon.writeinvalidate.cache;

import com.gmoon.cacheinvalidation.CacheEntry;

import java.util.List;

public interface CacheEvictable {

	List<CacheEntry> cacheEntries();
}
