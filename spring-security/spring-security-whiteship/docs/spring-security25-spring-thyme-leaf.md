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

### sec 네임 스페이스 추가

`xmlns:sec="http://www.thymeleaf.org/extras/spring-security"` 추가

````html

<html lang="en" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<!-- ... 생략 -->
</html>
````

코드 자동 완성 및 툴에 의존하여 뷰를 편리하게 개발할 수 있다.

````html

<html lang="en" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<body>
    <div sec:authorize-expr="isAuthenticated()">
        <h2 sec:authentication="name">Name</h2>
        <a href="/logout" th:href="@{/logout}">Logout</a>
    </div>
    
    <div sec:authorize-expr="!isAuthenticated()">
        <a href="/login" th:href="@{/login}">Login</a>
    </div>
</body>
</html>
````