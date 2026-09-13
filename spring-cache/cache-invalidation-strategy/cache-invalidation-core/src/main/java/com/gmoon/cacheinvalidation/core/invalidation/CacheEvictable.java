package com.gmoon.cacheinvalidation.core.invalidation;

import java.util.List;

public interface CacheEvictable {

	List<CacheKey> cacheEntriesToEvict();
}
