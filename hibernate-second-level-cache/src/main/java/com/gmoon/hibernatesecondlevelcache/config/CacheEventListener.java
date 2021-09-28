package com.gmoon.hibernatesecondlevelcache.config;

import lombok.extern.slf4j.Slf4j;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;

@Slf4j
public class CacheEventListener implements CacheEventLoggerListener<Object, Object> {

  @Override
  public void onCreated(Iterable<CacheEntryEvent<?, ?>> events) throws CacheEntryListenerException {
    logging(events);
  }

  @Override
  public void onExpired(Iterable<CacheEntryEvent<?, ?>> events) throws CacheEntryListenerException {
    logging(events);
  }

  @Override
  public void onRemoved(Iterable<CacheEntryEvent<?, ?>> events) throws CacheEntryListenerException {
    logging(events);
  }

  @Override
  public void onUpdated(Iterable<CacheEntryEvent<?, ?>> events) throws CacheEntryListenerException {
    logging(events);
  }
}
