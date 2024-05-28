# Spring data commons

### [HandlerMethodArgumentResolvers for Pageable and Sort](https://docs.spring.io/spring-data/commons/reference/repositories/core-extensions.html#core.web.basic.paging-and-sorting)

- Pageable
- PageableHandlerMethodArgumentResolver
- SortHandlerMethodArgumentResolver

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springjpapagination.global.domain.BasePageable;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRestController {

  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<UserContentListVO> findAll(BasePageable pageable) {
    return ResponseEntity.ok(userRepository.findAll(pageable));
  }
}
```

|      | description                                                                                                                                                                                                                                                                                                     |
|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| page | Page you want to retrieve. 0-indexed and defaults to 0.                                                                                                                                                                                                                                                         |
| size | Size of the page you want to retrieve. Defaults to 20.                                                                                                                                                                                                                                                          |
| sort | Properties that should be sorted by in the format property,property(,ASC or DESC)(,IgnoreCase). The default sort direction is case-sensitive ascending. Use multiple sort parameters if you want to switch direction or case sensitivity for example, `?sort=firstname&sort=lastname,asc&sort=city,ignorecase.` |

### Pageable 커스텀 확장 포인트

- PageableHandlerMethodArgumentResolverCustomizer
- SortHandlerMethodArgumentResolverCustomizer

`Pageable` 의 기본 동작을 커스텀 하기 위해선 `*Customizer` 인터페이스를 사용자 정의에 맞게 구현하여 `Bean`으로 등록한다.

```java

@Bean
public PageableHandlerMethodArgumentResolverCustomizer pageCustomizer() {
  return page -> page;
}

@Bean
public SortHandlerMethodArgumentResolverCustomizer sortCustomizer() {
  return sort -> sort;
}
```

만약 `MethodArgumentResolver`에 설정된 페이지 설정 관련 속성으로는 충분하지 않은 경우엔, `SpringDataWebConfiguration` 또는 HATEOAS 해당 항목을 확장하고 또는
pageableResolver(), sortResolver() 메서드를 재정의한다.

```java

@RequestMapping("/users")
@Controller
public class UserController {

  @GetMapping
  public String showUsers(
          Model model,
          // Pageable 요청에서 여러 인스턴스 Sort를 해결 해야 하는 경우.
          @Qualifier("thing1") Pageable first,
          @Qualifier("thing2") @PageableDefault(size = 10) Pageable second
  ) {

    model.add
    return "";
  }
} 
```

@PageableDefault 어노테이션을 사용해서 Pageable 기본 값 설정을 할 수 있다.

## Pageable config example

```java

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
public class WebConfig {

  @Bean
  public PageableHandlerMethodArgumentResolverCustomizer customizerPageableResolver() {
    return pageableResolver -> {
      pageableResolver.setPageParameterName("page");
      pageableResolver.setSizeParameterName("pageSize");
      pageableResolver.setOneIndexedParameters(false); // zero based page
      pageableResolver.setMaxPageSize(10);

      pageableResolver.setFallbackPageable(PageRequest.of(0, 15, Sort.unsorted()));
    };
  }
}
```

## Reference

- [spring.io - Spring Data](https://spring.io/projects/spring-data#learn)
  - [git - spring data commons](https://github.com/spring-projects/spring-data-commons)
  - [Core extensions](https://docs.spring.io/spring-data/commons/reference/repositories/core-extensions.html)
