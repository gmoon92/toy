package com.gmoon.hibernatesecondlevelcache.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class CacheUtil {

  @Autowired
  CacheManager cacheManager;

  public void evict(String cacheName, Object cacheKey) {
    cacheManager.getCache(cacheName).evict(cacheKey);
  }

  public void evictAll(String cacheName) {
    cacheManager.getCache(cacheName).clear();
  }
}