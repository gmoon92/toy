# 타임리프 스프링 시큐리티 확장팩

### 의존성 추가

```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity5</artifactId>
</dependency>
```

### Authentication과 Authorization 참조

```html
<div th:if="${#authorization.expr('isAuthenticated()')}">
    <h2 th:text="${#authentication.name}"></h2>
    <a href="/logout" th:href="@{/logout}">Logout</a>
</div>
<div th:unless="${#authorization.expr('isAuthenticated()')}">
    <a href="/login" th:href="@{/login}">Login</a>
</div>
```

`authorization` 표현식이 Type safe 하지 않는다는 단점이 존재.