package com.gmoon.hibernatesecondlevelcache.config;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

@Slf4j
public class CacheEventLoggerListener implements CacheEventListener<Object, Object> {

  @Override
  public void onEvent(CacheEvent<?, ?> event) {
    log.info("cache {} event. key: {}, oldValue: {}, newValue: {}", event.getType(), event.getKey(), event.getOldValue(), event.getNewValue());
  }
}
