# REST Assured

**`REST Assured`** 는 REST 웹 서비스의 [**`E2E Test(전 구간 테스트)`**](https://smartbear.com/solutions/end-to-end-testing/) 를 위한 테스트 라이브러리다.

BDD 스타일 테스트 코드를 작성할 수 있도록 설계되어 시나리오 기반의 이해하기 쉬운 테스트 코드를 구현할 수 있다.

## Environment

- org.springframework.boot:spring-boot-starter-parent:2.6.6
    - spring-boot-starter-test:2.6.6
        - org.hamcrest:hamcrest:2.2
- io.rest-assured:rest-assured:5.0.1
    - org.hamcrest:hamcrest:2.2
- io.rest-assured:spring-mock-mvc:5.0.1

## REST Assured

`REST Assured` 는 `HttpClient`를 사용하여 실제 Http 통신을하여 테스트 케이스를 검증한다.

웹서버 통신을 위해 `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` 사용하여 서블릿 컨테이너를 실행시킨 후에 테스트를 진행해야 한다.

- @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
- RestAssured.port

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
	
    @BeforeAll
    static void beforeAll(@LocalServerPort int port) {
        RestAssured.port = port;
    }
    
    @Test
    void testFindAll() {
        //@formatter:off
        RestAssured
            .given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .get("/user/list")
            .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("name", hasItems("gmoon", "guest"))
                .body("[0].name", equalTo("gmoon"))
                .body("[0].enabled", notNullValue());
        //@formatter:on
    }
}
```

## [REST Assured with Spring Mock MVC](https://github.com/rest-assured/rest-assured/wiki/GettingStarted#spring-mock-mvc)

`REST Assured` 2.2.0 버전 이상 부터 [`io.rest-assured:spring-mock-mvc`](https://github.com/rest-assured/rest-assured/wiki/Usage#spring-mock-mvc-module) 모듈을 사용하면 @WebMvcTest 에서 ResultAssured 스타일로 테스트 코드를 작성할 수 있다.

먼저 `io.rest-assured:spring-mock-mvc` 의존성을 추가하자.

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>spring-mock-mvc</artifactId>
    <version>5.0.1</version>
    <scope>test</scope>
</dependency>
```

- @WebMvcTest
- RestAssuredMockMvc.mockMvc(mockMvc)

````java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @MockBean
    UserService service;
    
    @BeforeAll
    static void beforeAll(@Autowired MockMvc mockMvc) {
        RestAssuredMockMvc.mockMvc(mockMvc); // MockMVC 주입
    }
    
    @Test
    void testFindAll() {
        Mockito.when(service.findAll())
            .thenReturn(Arrays.asList(User.from("gmoon"), User.from("guest")));
        
        //@formatter:off
        RestAssuredMockMvc
            .given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .get("/user/list")
            .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("name", hasItems("gmoon", "guest"))
                .body("[0].name", equalTo("gmoon"))
                .body("[0].enabled", notNullValue());
        //@formatter:on
    }
}
````

위 테스트 코드는 `REST Assured` 의 BDD 스타일로 테스트 코드가 작성되어 가독성이 높고, Spring Mock MVC 장점인 웹 어플리케이션을 서버에 배포하지 않고도 Controller 계층의 단위 테스트를 작성할 수 있다.

- BDD 스타일
- Spring Mock MVC

## Spring MockMVC VS REST Assured

확실히 REST Assured 테스트 코드는 MockMVC 테스트 코드에 비해 가독성이 높다.

```java
// MockMVC
@Test
void testFindAll() throws Exception {
    // when
    ResultActions result = mockMvc.perform(get("/user/list")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON));
    
    // then
    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.*.name").value(hasItems("gmoon", "guest")));
    result.andExpect(jsonPath("$.[0].name").value("gmoon"));
    result.andExpect(jsonPath("$.[0].enabled").exists());
}

// RestAssured
@Test
void testFindAll() {
    RestAssured
        .given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
        .when()
            .get("/user/list")
        .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .body("name", hasItems("gmoon", "guest"))
            .body("[0].name", equalTo("gmoon"))
            .body("[0].enabled", notNullValue());
}
```

## Reference

- [rest-assured.io](https://rest-assured.io/)
- [Github - REST Assured](https://github.com/rest-assured/rest-assured/wiki)
- [Baeldung - REST-assured](https://www.baeldung.com/rest-assured-tutorial)
  - [Baeldung - REST-assured Code](https://github.com/eugenp/tutorials/blob/master/testing-modules/rest-assured/README.md)
- [Baeldung - JSON Assert](https://www.baeldung.com/jsonassert)
- [Github - JsonPath](https://github.com/json-path/JsonPath)
- [Combine API and UI Testing For Confidence At Every Layer Of Your Application](https://smartbear.com/solutions/end-to-end-testing/)
