package com.gmoon.cacheinvalidation;

import java.util.List;

public interface CacheEvictable {

	List<CacheKey> cacheEntriesToEvict();
}
