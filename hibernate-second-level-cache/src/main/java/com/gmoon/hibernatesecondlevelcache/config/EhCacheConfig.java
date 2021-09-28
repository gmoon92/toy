package com.gmoon.hibernatesecondlevelcache.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class EhCacheConfig extends CacheConfig {

}
