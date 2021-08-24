# Spring Data Envers

### 1. Config

### 1.1. Dependency

Spring Data Envers를 사용하기 위해선 Spring Data JPA와 별개로 `spring-data-envers` 의존성을 추가해줘야 한다.

```xml
<dependency>
  <groupId>org.springframework.data</groupId>
  <artifactId>spring-data-envers</artifactId>
  <version>2.5.4</version>
</dependency>
```

### 1.2. Java Config

Spring Data JPA와 Spring Data Envers를 활성화 하기 위해선 `repositoryFactoryBeanClass` 를 추가해줘야 한다.

````java
@Configuration
@EnableEnversRepositories
@EnableTransactionManagement
public class JpaEnversConfig {

  @Autowired
  DataSource dataSource;

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(true);

    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan("example.springdata.jpa.envers");
    factory.setDataSource(dataSource());
    return factory;
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager txManager = new JpaTransactionManager();
    txManager.setEntityManagerFactory(entityManagerFactory);
    return txManager;
  }
}
````

### 1.3 EnversRevisionRepositoryFactoryBean 설정

```java
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.gmoon.**",
        repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@EnableTransactionManagement
public class JpaConfig {
  
  // ... 생략
}
```

`@EnableJpaRepositories(repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)` 추가

- repositoryFactoryBeanClass: Repository interface 를 스프링이 자동으로 bean 으로 생성해주는 클래스 타입 지정
  - 아무 설정이 없다면 JpaRepositoryFactoryBean에 의해 빈 등록
- EnversRevisionRepositoryFactoryBean: JpaRepositoryFactoryBean 를 확장한 리비전 Repository Factory Bean 클래스

````java
public class EnversRevisionRepositoryFactoryBean<T extends RevisionRepository<S, ID, N>, S, ID, N extends Number & Comparable<N>>
		extends JpaRepositoryFactoryBean<T, S, ID> { // ㅇㄷ
  
  // ... 이하 생략
}
````


``` java
package org.springframework.data.repository.history;

@NoRepositoryBean
public interface RevisionRepository<T, ID, N extends Number & Comparable<N>>
                    extends Repository<T, ID> {

    // 최근 리비전 조회(현 데이터에 근접)
	Optional<Revision<N, T>> findLastChangeRevision(ID id);

	// 엔티티 ID에 해당하는 모든 히스토리 조회
	Revisions<N, T> findRevisions(ID id);

	// 히스토리 페이징 + 정렬 조회
	Page<Revision<N, T>> findRevisions(ID id, Pageable pageable);

	// 특정 리비전 조회
	Optional<Revision<N, T>> findRevision(ID id, N revisionNumber);
}
```

- T: Entity Class Type
- ID: Entity Id Class Type
- N: RevisionNumber Class Type (default Integer)
  - `RevisionNumber` 기본 타입은 Integer, 일반적으로 커스텀하여 Long Type으로 변경하여 구성

## RevisionRepository 인터페이스 적용

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

public interface MemberRepository extends
        JpaRepository<Member, Long>,
        RevisionRepository<Member, Long, Integer>
{
  // ... 생략
}
```

## 단순 조회

```java
public class MemberService {
  
  public Member findRevision(Member currentMember, Integer revisionNumber) {
    Revision<Integer, Member> revision = memberRepository.findRevision(member.getId(), 1);

    Member entity = revision.getEntity(); // Entity
    Integer revisionNumber = revision.getRevisionNumber(); // revision
    DateTime dateTime = revision.getRevisionDate(); // revision date
    return entity;
  }
}
```

## 페이징, 정렬 조회


```java
public class MemberService {
  
  public Page<Revision<Integer, Member>> getPage(Member currentMember, Page page) {
    
    Page<Revision<Integer, Member>> result = memberRepository.findRevisions(member.getId(), new PageRequest(0, 10), RevisionSort.desc());

    result.getTotalElements(); // 전체 수
    result.getContent(); // 내용
    return result;
  }
}
```

Spring Data Envers 단점

- 복잡한 조회 -> 하이버네이트 Envers 직접 사용
- 버전업이 잘 안되는 편이다.(사실 기능이 별로 없다.)
- 스프링 데이터가 지원하는 QueryDsl관련 기능과 함께 사용하려면 코드를 약간 수정해야 한다.
  - https://github.com/spring-projects/spring-data-envers/issues/30
    - https://gist.github.com/tanapoln/af4b4a40e5472f0c8e9625d31f7d2153
  - EnversRevisionRepositoryFactoryBean 커스텀 확정
    - QueryDsl interface forwarding 가능

### 참고

- https://docs.spring.io/spring-data/envers/docs/current/reference/html/#reference