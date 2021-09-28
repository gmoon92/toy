## EHCache3

## dependencies

- spring-boot-starter-cache
- javax.cache:cache-api
- org.ehcache:ehcache
  - ehcache 3버전 부터는 `javax.cache:cache-api` 포함되어져 있다. 

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
        <version>2.5.5</version>
    </dependency>
    <dependency>
        <groupId>org.ehcache</groupId>
        <artifactId>ehcache</artifactId>
        <version>3.9.6</version>
    </dependency>
</dependencies>
```

## config

## evict

- cacheManager
- @CacheEvict  

## The Ehcache 3.x JSR-107 Provider

jcache provider

## listener add 

## xml -> jsr config


## 참고

- https://www.baeldung.com/spring-boot-evict-cache