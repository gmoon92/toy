# JPA Second Level Cache

## 1. 프로젝트 환경

- Spring Boot
- H2 in-memory
- Hibernate 5.4.32.Final

## 2. 학습 내용

본 내용엔 EHCache의 전반적인 개념과 설정 방법 및 사용법 그리고 EHCache 2 버전과 3 버전의 차이점에 대해서도 다룰 예정이다.

## 3.1. EHCache

첫 번째로 알아볼 2차 캐시는 EHCache 관련하여 알아보자.

### 3.1.1. Dependency

- spring-boot-starter-cache
- ehcache (net.sf.ehcache)
- cache-api (javax.cache)

```xml
<dependencies>
    <dependency>
    <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    
    <dependency>
        <groupId>javax.cache</groupId>
        <artifactId>cache-api</artifactId>
    </dependency>

    <dependency>
        <groupId>net.sf.ehcache</groupId>
        <artifactId>ehcache</artifactId>
    </dependency>
</dependencies>
```


참고로 JPA 구현체로 하이버네이트를 사용하고 있다면, `hibernate-jcache`, `hibernate-ehcache` 를 추가하면 된다.

```xml
<!-- javax.cache:cache-api -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-jcache</artifactId>
    <version>${hibernate.version}</version>
</dependency>

<!-- net.sf.ehcache:ehcache -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-ehcache</artifactId>
    <version>${hibernate.version}</version>
</dependency>
```

### 참고

- https://www.baeldung.com/hibernate-second-level-cache
- https://www.baeldung.com/spring-boot-ehcache
- https://www.ehcache.org/generated/2.9.0/html/ehc-all/
- https://www.ehcache.org/documentation/3.3/migration-guide.html