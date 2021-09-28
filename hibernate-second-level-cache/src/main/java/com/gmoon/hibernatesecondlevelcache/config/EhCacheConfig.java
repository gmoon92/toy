package com.gmoon.hibernatesecondlevelcache.config;

import com.gmoon.hibernatesecondlevelcache.member.Member;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

@Configuration
@EnableCaching
public class EhCacheConfig extends CacheConfig {

  @Bean
  public CacheManager createManager() {
    CachingProvider provider = new EhcacheCachingProvider();
    CacheManager manager = provider.getCacheManager();

    // config
    MutableConfiguration configuration = new MutableConfiguration<Long, Member>()
            .setTypes(Long.class, Member.class)
            .setStoreByValue(false)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));

    // listener config
    MutableCacheEntryListenerConfiguration listenerConfiguration =
            new MutableCacheEntryListenerConfiguration(FactoryBuilder.factoryOf(CacheEventLoggerListener.class),
                    null,
                    true,
                    true
            );
    configuration.addCacheEntryListenerConfiguration(listenerConfiguration);

    // add cache
    manager.createCache(MEMBER_FIND_BY_ID, configuration);
    return manager;
  }
}
