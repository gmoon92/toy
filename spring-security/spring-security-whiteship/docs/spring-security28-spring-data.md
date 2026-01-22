# 스프링 데이터 연동

https://docs.spring.io/spring-security/site/docs/current/reference/html5/#data

@Query 애노테이션에서 SpEL로 principal 참조할 수 있는 기능 제공.

### 스프링 시큐리티 데이터 의존성 추가

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-data</artifactId>
    <version>${spring-security.version}</version>
</dependency>
```

### @Query에서 principal 사용하기

```java
@Query("select b from Book b where b.author.id = ?#{principal.account.id}")
List<Book> findCurrentUserBooks();
```

### 타임리프 리스트 참조

```javascript
<tr th:each="book : ${books}">
    <td><span th:text="${book.title}"> Title </span></td>
</tr>
```

