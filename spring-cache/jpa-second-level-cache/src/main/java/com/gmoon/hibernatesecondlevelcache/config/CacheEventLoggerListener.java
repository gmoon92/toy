package com.gmoon.hibernatesecondlevelcache.config;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.EventType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheEventLoggerListener implements CacheEventListener<Object, Object> {

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

	private void logging(Iterable<CacheEntryEvent<?, ?>> events) {
		events.forEach(event -> {
			EventType eventType = event.getEventType();
			Object key = event.getKey();
			Object oldValue = event.getOldValue();
			Object value = event.getValue();
			log.info("cache {} event. key: {}, oldValue: {}, newValue: {}", eventType, key, oldValue, value);
		});
	}
}
