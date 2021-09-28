package com.gmoon.hibernatesecondlevelcache.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.event.EventType;

interface CacheEventLoggerListener<K, V> extends CacheEntryCreatedListener<K, V>,
        CacheEntryRemovedListener<K, V>,
        CacheEntryUpdatedListener<K, V>,
        CacheEntryExpiredListener<K, V> {

  Logger log = LoggerFactory.getLogger("com.gmoon.hibernatesecondlevelcache.config.BaseCacheEventLoggerListener");

  default void logging(Iterable<CacheEntryEvent<?,?>> events) {
    events.forEach(event -> {
      EventType eventType = event.getEventType();
      Object key = event.getKey();
      Object oldValue = event.getOldValue();
      Object value = event.getValue();
      log.info("cache {} event. key: {}, oldValue: {}, newValue: {}", eventType, key, oldValue, value);
    });
  }
}
