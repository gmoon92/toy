package com.gmoon.writeinvalidate.cache;

import com.gmoon.cacheinvalidation.CacheKey;

import java.util.List;

public interface CacheEvictable {

	List<CacheKey> cacheKeys();
}
