package com.gmoon.hibernatesecondlevelcache.config;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;

interface CacheEventListener<K, V> extends CacheEntryCreatedListener<K, V>,
        CacheEntryRemovedListener<K, V>,
        CacheEntryUpdatedListener<K, V>,
        CacheEntryExpiredListener<K, V> {

}
