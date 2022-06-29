# Mockito

## Mockito inline

Mockito 3.4.0 버전 이상부터 지원

`Mockito inline`은 정적 메서드 mocking 을 지원한다.  
유틸 클래스 mocking

### Dependency

```text
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-inline</artifactId>
    <version>3.4.0</version>
    <scope>test</scope>
</dependency>
```

### Test

정적 메서드를 mocking 시에 메서드 적용 범위를 지정하는게 중요하다.

- Mockito#close()

```java
class StringUtilsTest {

	static MockedStatic<StringUtils> stringUtils;

	@BeforeAll
	static void beforeAll() {
		stringUtils = Mockito.mockStatic(StringUtils.class);
	}

	@AfterAll
	static void afterAll() {
		stringUtils.close();
	}
}
```

### try-with-resource

`MockedStatic`는 `AutoCloseable` 확장한 클래스로 `try-with-resource` 로 mocking된 정적 메서드의 적용 범위를 지정한다.

```java
class StringUtilsTest {

	@DisplayName("utils class mocking")
	@Test
	void testMockingStaticMethods() {
		try (MockedStatic<StringUtils> utils = Mockito.mockStatic(StringUtils.class)) {
			String name = "gmoon";

			utils.when(() -> StringUtils.isEmpty(name))
				.thenReturn(true);

			assertThat(StringUtils.isEmpty("gmoon2")).isFalse();
			assertThat(StringUtils.isEmpty(name)).isTrue();
		}
	}

	static final class StringUtils {
		private StringUtils() {}
		
		static boolean isEmpty(String str) {
			return str == null || str.length() == 0;
		}
	}
}
```

## Reference

- [baeldung - mockito mock static methods](https://www.baeldung.com/mockito-mock-static-methods)
