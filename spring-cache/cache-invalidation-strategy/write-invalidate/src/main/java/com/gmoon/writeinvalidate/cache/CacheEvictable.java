package com.gmoon.writeinvalidate.cache;

import java.util.Set;

/**
 * 커밋되면 지워야 할 캐시 키를 엔티티가 스스로 밝힌다.
 */
public interface CacheEvictable {

	Set<CacheKey> cacheKeys();
}
