# JPA Second Level Cache

## Environment

- Spring Boot
- H2 in-memory
- Hibernate 5.6.3.Final
- org.ehcache3:ehcache:3.9.6
- com.hazelcast:hazelcast:4.2.2
- org.redisson:redisson:3.17.7

## 1. EHCache

### 1.1. Dependency

- org.springframework.boot:spring-boot-starter-cache
- net.sf.ehcache:ehcache (EHCache 2)
- javax.cache:cache-api

참고로 JPA 구현체로 하이버네이트를 사용하고 있다면, `hibernate-jcache`, `hibernate-ehcache` 를 추가하면 된다.

- org.hibernate:hibernate-jcache:${hibernate.version}
    - javax.cache:cache-api 포함
- org.hibernate:hibernate-ehcache:${hibernate.version}
    - net.sf.ehcache:ehcache

### 1.2. EHCache2 기반 XML Config

- ehcache.xml

```xml

<config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.ehcache.org/v3"
        xsi:schemaLocation="http://www.ehcache.org/v3 http://www.ehcache.org/schema/ehcache-core-3.0.xsd">

    <cache alias="MEMBER_FIND_BY_ID">
        <key-type>java.lang.Long</key-type>
        <value-type>com.gmoon.hibernatesecondlevelcache.member.Member</value-type>
        <expiry>
            <ttl unit="seconds">30</ttl>
        </expiry>

        <listeners>
            <listener>
                <class>com.gmoon.hibernatesecondlevelcache.config.CacheEventLoggerListener</class>
                <event-firing-mode>ASYNCHRONOUS</event-firing-mode>
                <event-ordering-mode>UNORDERED</event-ordering-mode>
                <events-to-fire-on>CREATED</events-to-fire-on>
                <events-to-fire-on>EXPIRED</events-to-fire-on>
            </listener>
        </listeners>

        <resources>
            <heap unit="entries">2</heap>
            <offheap unit="MB">10</offheap>
        </resources>
    </cache>

</config>
```

### 1.3. EHCache3 JCache Java Config

```java
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.hibernate.cache.jcache.ConfigSettings;

import org.springframework.boot.autoconfigure.cache.CacheType;

@Configuration
public class JCacheConfig {

	@Bean
	public CacheManager jCacheManager() {
		CachingProvider cacheProvider = new EhcacheCachingProvider();
		CacheManager jCacheManager = cachingProvider.getCacheManager();

		// add cache
		createJCaches(jCacheManager);
		return jCacheManager;
	}

	private void createJCaches(CacheManager jCacheManager) {
		for (CachePolicy policy : CachePolicy.values()) {
			String cacheName = policy.getCacheName();
			jCacheManager.createCache(cacheName, getJCacheConfig(policy.getTtl()));
		}
	}

	private MutableConfiguration<Long, Object> getJCacheConfig(Duration ttl) {
		MutableConfiguration<Long, Object> config = new MutableConfiguration<Long, Object>()
		  //            .setTypes(Long.class, Member.class)
		  .setStoreByValue(false)
		  .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(ttl));

		// add listener config
		config.addCacheEntryListenerConfiguration(createCacheEventLoggerListenerConfig());
		return config;

	}

	private CacheEntryListenerConfiguration<Long, Object> createCacheEventLoggerListenerConfig() {
		return new MutableCacheEntryListenerConfiguration<>(
		  FactoryBuilder.factoryOf(CacheEventLoggerListener.class),
		  null,
		  true,
		  false
		);
	}
}
```

## 2. Hazelcast

### 2.1. Dependency

- com.hazelcast:hazelcast:3.12.13

### 2.2. JCache Provider Config

JVM Option 으로 설정하는 방법

```text
## HazelcastCachingProvider
## jvm opt
-Dhazelcast.jcache.provider.type=[client|server]
```

- client, member, server
    - client: HazelcastClientCachingProvider(default)
    - member, server: HazelcastServerCachingProvider

```java
public class JCacheConfig {
	public CacheManager jCacheManager() {

		CachingProvider provider
		  // Hazelcast Embedded Server
		  = new HazelcastServerCachingProvider();

		// Hazelcast server/client
		// = new com.hazelcast.cache.HazelcastCachingProvider();
		// = new com.hazelcast.client.cache.HazelcastClientCachingProvider();

		// ...
		return jCacheManager;
	}
}
```

### 3. Custom JCache Provider

하이버네이트의 l2 캐싱 사용 옵션이 활성화가 되어 있다면, 스프링은 부트스트랩 로드 시점에 Class Path 에 `javax.cache.spi.CachingProvider` 인터페이스를 구현한 클래스를 찾는다.

해즐캐스트나 레디슨 라이브러리와 같이 내부적으로 `javax.cache.spi.CachingProvider`를 구현된 클래스를 포함하고 있다면, LocalContainerEntityManager 빈 등록 시점에 캐시
제공자를 명시하라는 에러가 발생한다.

```text
Multiple CachingProviders have been configured when only a single CachingProvider is expected
```

- org.ehcache:ehcache
    - org.ehcache.jsr107.EhcacheCachingProvider
- com.hazelcast:hazelcast
    - com.hazelcast.cache.HazelcastCachingProvider
    - com.hazelcast.cache.HazelcastServerCachingProvider
    - com.hazelcast.cache.HazelcastClientCachingProvider
- org.redisson:redisson
    - org.redisson.jcache.JCachingProvider

````java
package org.hibernate.cache.jcache.internal;

public class JCacheRegionFactory extends RegionFactoryTemplate {

	protected CacheManager resolveCacheManager(SessionFactoryOptions settings, Map properties) {
		final Object explicitCacheManager = properties.get(ConfigSettings.CACHE_MANAGER);

		// [1] 커스텀한 JCache Manager 사용 
		if (explicitCacheManager != null) {
			return useExplicitCacheManager(settings, explicitCacheManager);
		}

		// [2] 지정한 캐시 매니저가 없다면 CachingProvider 를 통해 생성 
		final CachingProvider cachingProvider = getCachingProvider(properties);
		final CacheManager cacheManager;
		final URI cacheManagerUri = getUri(settings, properties);
		if (cacheManagerUri != null) {
			cacheManager = cachingProvider.getCacheManager(cacheManagerUri, getClassLoader(cachingProvider));
		} else {
			cacheManager = cachingProvider.getCacheManager(cachingProvider.getDefaultURI(),
			  getClassLoader(cachingProvider));
		}
		return cacheManager;
	}
}
````

해결 방안으로 두 가지 방안이 존재한다.

1. CachingProvider 프로퍼티 설정
    - hibernate.cache.javax.cache.provider
    - 기존 캐싱 라이브러리에서 제공하는 캐싱 제공자 FQCN(Fully Qualified Class Name) 을 명시하는 방법
2. CacheManager 프로퍼티 설정
    - hibernate.javax.cache.cache_manager
    - HibernatePropertiesCustomizer 설정

첫 번째는 'spring.jpa.properties.hibernate.javax.cache.provider' 에 프로젝트에서 사용할 캐싱 제공자 객체의 FQCN을 명시한다.

```yaml
spring:
  jpa:
    properties:
      hibernate:
        ## Hibernate L2 Cache
        cache:
          "[use_second_level_cache]": true
          "[use_query_cache]": true
          #          "[ehcache.missing_cache_strategy]": create
          ## 캐싱 구현체 지정
          "[region.factory_class]": jcache

          ## 캐싱 제공자 지정
          "[javax.cache.provider]": org.ehcache.jsr107.EhcacheCachingProvider
```

### [Using a non-default JCache CacheManager](https://github.com/hibernate/hibernate-orm/blob/main/documentation/src/main/asciidoc/userguide/chapters/caching/Caching.adoc#using-a-non-default-jcache-cachemanager)

두 번째 방식은 `hibernate.javax.cache.cache_manager` 프로퍼티에 캐시 매니저를 지정해주는 방식이다.

```text
Using a non-default JCache CacheManager
If you don’t want to use the default CacheManager, you need to set the hibernate.javax.cache.cache_manager configuration property to one of the following values:

Object reference
If the value is an Object instance implementing the CacheManager interface, the provided CacheManager instance will be used.
```

- hibernate.javax.cache.cache_manager

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.hibernate.cache.jcache.ConfigSettings;

@Configuration
public class JCacheConfig {

  @Bean
  public HibernatePropertiesCustomizer hibernateSecondLevelCacheCustomizer() {
    return (properties) -> properties.put(
            ConfigSettings.CACHE_MANAGER, // hibernate.javax.cache.cache_manager
            jCacheManager() // 
    );
  }

  @Bean
  public javax.cache.CacheManager jCacheManager() {
    CachingProvider cacheProvider = new EhcacheCachingProvider();
    CacheManager jCacheManager = cacheProvider.getCacheManager();

    // add cache
    createJCaches(jCacheManager);
    return jCacheManager;
  }
}
```

## JCache Properties

- [javax.cache.missing_cache_strategy](https://github.com/hibernate/hibernate-orm/blob/main/documentation/src/main/asciidoc/userguide/chapters/caching/Caching.adoc#jcache-missing-cache-strategy)
  - `fail`: Fail with an exception on missing caches.
  - `create-warn`: Default value. Create a new cache when a cache is not found (see create below), and also log a warning
    about the missing cache.
  - `create`: Create a new cache when a cache is not found, without logging any warning about the missing cache.

## Reference

- Spring
    - [Baeldung - Hibernate Second Level Cache](https://www.baeldung.com/hibernate-second-level-cache)
    - [Baeldung - EHCache](https://www.baeldung.com/spring-boot-ehcache)
    - [토리맘 - Spring Caching](https://godekdls.github.io/Spring%20Boot/caching/)
- EHCache
    - [EHCache 3 - Migration Guide](https://www.ehcache.org/documentation/3.3/migration-guide.html)
    - [EHCache 2 - Documentation Library](https://www.ehcache.org/generated/2.9.0/html/ehc-all/)
- Hazelcast
    - [Hazelcast - JCache](https://docs.hazelcast.com/imdg/3.12/jcache/overview)
    - [Hazelcast - JCache setup](https://docs.hazelcast.com/hazelcast/5.2/jcache/setup)
    - [Hazelcast - Configuration](https://docs.hazelcast.com/hazelcast/latest/configuration/understanding-configuration)
- Hibernate
  - [Hibernate - Caching](https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/chapters/caching/Caching.html)
  - [Hibernate - Caching Config Properties](https://docs.jboss.org/hibernate/orm/5.0/userguide/html_single/Hibernate_User_Guide.html#caching-config-properties)
