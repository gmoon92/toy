# QuickPerf Testing with Junit5

**`QuickPerf`** 는 오픈소스 테스팅 라이브러리다.

`Java Performance 검증` 및 Hibernate 에서 `실제로 쿼리가 몇 번 수행됐는지` 테스트 코드로 검증할 수 있도록 지원한다.

QuickPerf 의 활용법은 다양하지만, 특히 잘못된 도메인 연관 관계 구조를 파악한다거나, N+1이 발생하는 조회 쿼리 튜닝할 때 용이하다.

> QuickPerf is a testing library for Java to quickly evaluate and improve some performance-related properties

## 1. dependency

- org.quickperf:quick-perf-junit5
- org.quickperf:quick-perf-springboot2-sql-starter

```xml
<dependency>
	<groupId>org.quickperf</groupId>
	<artifactId>quick-perf-junit5</artifactId>
	<version>1.1.0</version>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>org.quickperf</groupId>
	<artifactId>quick-perf-springboot2-sql-starter</artifactId>
	<version>1.1.0</version>
	<scope>test</scope>
</dependency>
```

## 2. Config

### 2.1. Junit5 Extension Config

설정 방법은 두 가지로 @ExtendWith 방법을 추천한다.

- junit-platform.properties
- **`@ExtendWith`**

### 2.1.1. junit-platform.properties

```text
# src/test/resources/junit-platform.properties
junit.jupiter.extensions.autodetection.enabled=true
```

### 2.1.2. @ExtendWith Annotation

```java
@ExtendWith(org.quickperf.junit5.QuickPerfTestExtension.class)
```

### 2.2.1. @DataJpaTest with QuickPerf

- **`@Import org.quickperf.spring.sql.QuickPerfSqlConfig`**
- @OverrideAutoConfiguration(enabled = true)

```java
@DataJpaTest
@ExtendWith(QuickPerfTestExtension.class)
@Import(QuickPerfSqlConfig.class)
class BasicRepositoryTest { }

@DataJpaTest
@ExtendWith(QuickPerfTestExtension.class)
@OverrideAutoConfiguration(enabled = true)
class BasicRepositoryTest { }
```

## 2.2.2. @SpringBootTest with QuickPerf

```java
@SpringBootTest
@ExtendWith(QuickPerfTestExtension.class)
class BasicServiceTest { }
```

## 3. Sample Test Code

```java
@DataJpaTest
@ExtendWith(QuickPerfTestExtension.class)
@Import(QuickPerfSqlConfig.class)
class TeamRepositoryTest extends InitTestDataExecutor {
  @Autowired
  TeamRepository repository;

  @Test
  @ExpectSelect(7)
  void testFindAll() {
    // given
    List<Team> teams = repository.findAll();

    // when then
    for (Team team : teams) {
      for (Member member : team.getMembers()) {
        MemberOption memberOption = member.getMemberOption();
        assertThat(memberOption.isEnabled()).isTrue();
      }
    }
  }
}
```

이외 다양한 테스트 샘폴 코드는 [Github QuickPerf](https://github.com/quick-perf/quickperf-examples) 를 참고하자.

## 4. QuickPerf @Annotations

`org.quickperf.sql.annotation.*` 패키지의 어노테이션을 참고하자.

> [QuickPerf Github - wiki](https://github-wiki-see.page/m/quick-perf/doc/wiki_index)

- **`@ExpectInsert`**
- **`@ExpectMaxInsert`**
- **`@ExpectUpdate`**
- **`@ExpectUpdatedColumn`**
- **`@ExpectMaxUpdate`**
- **`@ExpectMaxUpdatedColumn`**
- **`@ExpectSelect`**
- **`@ExpectSelectedColumn`**
- **`@ExpectMaxSelect`**
- **`@ExpectMaxSelectedColumn`**
- **`@ExpectDelete`**
- **`@ExpectMaxDelete`**

## Reference

- [GitHub QuickPerf](https://github.com/quick-perf/quickperf)
  - [GitHub QuickPerf - JUnit5 Dependencies](https://github.com/quick-perf/doc/wiki/JUnit-5#dependencies)
  - [GitHub QuickPerf - Hibernate Junit5](https://github.com/quick-perf/quickperf-examples/tree/master/hibernate-junit5)
  - [GitHub QuickPerf - Spring Junit5](https://github.com/quick-perf/doc/wiki/Spring#junit-5)
  - [GitHub QuickPerf - Spring Junit5 QuickPerf](https://github-wiki-see.page/m/quick-perf/doc/wiki/Spring#junit-5)
  - [GitHub QuickPerf - Springboot Junit5](https://github.com/quick-perf/quickperf-examples/tree/master/springboot-junit5)
  - [programtalk.com - Sample codes](https://programtalk.com/vs3/?source=java/quick-perf/quickperf/sql/sql-annotations/src/main/java/org/quickperf/sql/annotation/ExpectSelectedColumn.java)
- [Junit5 - Gradle config params](https://junit.org/junit5/docs/current/user-guide/#running-tests-build-gradle-config-params)
- [Junit5 - Maven config params](https://junit.org/junit5/docs/current/user-guide/#running-tests-build-maven-config-params)
- [Junit5 Extensions - @ExtendWith](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-declarative)
