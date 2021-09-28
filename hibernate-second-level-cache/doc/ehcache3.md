## EHCache3

## dependencies

- spring-boot-starter-cache
- javax.cache:cache-api
- org.ehcache:ehcache

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
    <dependency>
        <groupId>javax.cache</groupId>
        <artifactId>cache-api</artifactId>
        <version>1.1.1</version>
    </dependency>
</dependencies>
```

## config


## evict

- cacheManager
- @CacheEvict  


- https://www.baeldung.com/spring-boot-evict-cache