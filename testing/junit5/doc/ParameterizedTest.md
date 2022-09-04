# ParameterizedTest

JUnit5 는 인수 테스트를 지원하기 위해 @ParameterizedTest 어노테이션을 제공한다.

@ParameterizedTest 메서드는 static 메서드와 private 접근제한자가 아니어야 한다.

## @ArgumentsSource

@ParameterizedTest 메서드는 @ArgumentsSource 어노테이션이 지정된 어노테이션을 추가로 지정해야 한다.

> `ParameterizedTestExtension` 클래스의 provideTestTemplateInvocationContexts 메서드를 살펴보자.

- [@CsvFileSource](#@csv-file-source)
- [@CsvSource](#@csv-source)
- [@EnumSource](#@enum-source)
- [@NullSource](#@null-source)
- [@EmptySource](#@empty-source)
- [@NullAndEmptySource](#@null-and-empty-source)
- [@ValueSource](#@value-source)
- [@MethodSource][#@method-source]
    - [External MethodSource](#external-method-source)
- [ArgumentsAccessor](#arguments-accessor)
- [@AggregateWith](#@aggregate-with)
- [@ConvertWith](#@convert-with)
- [Custom @ArgumentsSource](#custom-@ArgumentsSource)

### @CsvFileSource

@CsvFileSource 는 CSV 파일의 인수 테스트를 지원한다.

```text
name|enabled
gmoon|true
super, moon|false
guest|false
```

> src/test/resources/user.csv

```java
class JupiterParameterizedTest {

  @DisplayName("사용자 CSV 파일, 사용자 활성화 여부")
  @ParameterizedTest(name = "[{index}] {0}({1})... arguments: {arguments}")
  @CsvFileSource(resources = "/user.csv", delimiter = '|', numLinesToSkip = 1)
  void csvFilesSource(String username, Boolean enabled) {
    assumingThat(Boolean.TRUE == enabled,
      () -> assertThat(userName).isEqualTo("gmoon")
    );

    assumingThat(Boolean.FALSE == enabled,
      () -> assertThat(userName)
        .containsAnyOf("guest", "super, moon")
    );
  }
}
```

### @CsvSource

@CsvSource 는 CSV 파일 없이 CSV 데이터 형식의 인수를 지원한다.

```java
class JupiterParameterizedTest {

  @DisplayName("사원 CSV 데이터 검증")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @CsvSource(value = {
    "gmoon|true",
    "'super, moon'|false",
    "guest|false"
  }, delimiter = '|')
  void csvSource(String userName, Boolean enabled) {
    assumingThat(Boolean.TRUE == enabled,
      () -> assertThat(userName).isEqualTo("gmoon")
    );

    assumingThat(Boolean.FALSE == enabled,
      () -> assertThat(userName)
        .containsAnyOf("guest", "super, moon")
    );
  }
}
```

### @EnumSource

@EnumSource 는 enum 클래스 타입 인수 테스트를 지원한다.

```java

@RequiredArgsConstructor
@Getter
public enum Role {

  ADMIN(0),
  MANAGER(1),
  USER(2);

  private final int value;

  public static final Map<Integer, Role> ALL = Stream.of(values())
    .collect(collectingAndThen(
      toMap(Role::getValue, Function.identity()),
      Collections::unmodifiableMap
    ));
}
```

```java
class JupiterParameterizedTest {

  @DisplayName("이넘 클래스 인수 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @EnumSource(Role.class)
  void enumSource(Role role) {
    // given
    int value = role.getValue();

    // when then
    assertThat(Role.ALL.get(value))
      .isEqualTo(role);
  }
}
```

### @NullSource

@NullSource 은 null 인수 테스트를 지원한다.

```java
class JupiterParameterizedTest {

  @DisplayName("Null 인수 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @NullSource
  void nullSource(String actual) {
    assertThat(Objects.isNull(actual)).isTrue();
  }
}
```

### @EmptySource

@EmptySource 는 다음 타입의 비어 있는 인수를 지원한다.

- java.lang.String
- java.util.List
- java.util.Set
- java.util.Map
- primitive arrays (e.g., int[], char[][], etc.)
- object arrays (e.g.,String[], Integer[][], etc.)

````java
class JupiterParameterizedTest {

  @DisplayName("empty 인수 테스트")
  @Nested
  class EmptySourceTest {

    @DisplayName("빈 문자열")
    @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
    @EmptySource
    void case1(String actual) {
      assertThat(actual).isBlank();
    }

    @DisplayName("빈 리스트")
    @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
    @EmptySource
    void case2(List<String> actual) {
      assertThat(actual).isEmpty();
    }

    @DisplayName("빈 원시 타입 배열")
    @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
    @EmptySource
    void case3(byte[] actual) {
      assertThat(actual).isEmpty();
    }

    @DisplayName("빈 배열")
    @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
    @EmptySource
    void case4(Integer[] actual) {
      assertThat(actual).isEmpty();
    }
  }
}
````

### @NullAndEmptySource

@NullAndEmptySource 는 @NullSource 와 @EmptySource 를 합친 인수 테스트를 지원한다.

예를 들어 빈 문자열을 테스트 케이스를 작성한다고 가정해보자.

```java
class JupiterParameterizedTest {

  @DisplayName("null 또는 빈 문자열")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @NullSource
  @EmptySource
  void nullAndEmptySource(String actual) {
    assertThat(actual).isEmpty();
  }
}
```

@NullSource 와 @EmptySource 를 사용해서 복합적으로 인수 테스트를 할 수 있지만, @NullAndEmptySource 를 사용하면 null 과 empty 인수 테스트를 사용할 수 있다.

```java
class JupiterParameterizedTest {

  @DisplayName("null 또는 빈 문자열")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @NullAndEmptySource
  void nullAndEmptySource(String actual) {
    assertThat(StringUtils.isBlank(actual)).isTrue();
  }
}
```

### @ValueSource

@ValueSource 는 다음 인수 타입을 지원한다.

- short
- byte
- int
- long
- float
- double
- char
- boolean
- java.lang.String
- java.lang.Class

```java
class JupiterParameterizedTest {

  @DisplayName("값 인수 테스트")
  @Nested
  class ValueSourceTest {

    @InjectMocks
    MemberService memberService;

    @Mock
    MemberRepository memberRepository;

    @DisplayName("값 인수 테스트")
    @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
    @ValueSource(ints = { 1, 2, 3 })
    void case1(int actual) {
      assertThat(actual > 0 && actual < 4).isTrue();
    }

    @DisplayName("예외 클래스 테스트")
    @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
    @ValueSource(classes = { IllegalArgumentException.class, EntityNotFoundException.class,
      PersistenceException.class })
    void case2(Class<? extends RuntimeException> exception) {
      // given
      Long memberId = 0L;

      // when
      when(memberRepository.findById(memberId))
        .thenThrow(exception);

      // then
      assertThatThrownBy(() -> memberService.disable(memberId))
        .isInstanceOf(SystemException.class);
    }
  }
}
```

### @MethodSource

@MethodSource 는 Stream API 를 활용한 인수 테스트를 지원한다.

- Stream
- DoubleStream
- IntStream
- LongStream

```java
class JupiterParameterizedTest {

  @DisplayName("문자열 인수 테스트")
  @ParameterizedTest
  @MethodSource("stringProvider")
  void methodSource(String actual) {
    assertThat(actual).isNotNull();
  }

  static Stream<String> stringProvider() {
    return Stream.of("gmoon", "guest");
  }

  @DisplayName("음수 인수 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @MethodSource("negativeNumberProvider")
  void methodSource2(Integer actual) {
    assertThat(actual < 0).isTrue();
  }

  static IntStream negativeNumberProvider() {
    return IntStream.range(-10, 0).skip(0);
  }

  @DisplayName("다양한 인수 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @MethodSource("userProvider")
  void methodSource3(String userName, Role role, boolean enabled) {
    assertThat(userName).isNotNull();
    assertThat(role).isInstanceOf(Role.class);
    assertThat(enabled).isInstanceOf(Boolean.class);
  }

  static Stream<Arguments> userProvider() {
    return Stream.of(
      Arguments.of("gmoon", Role.ADMIN, true),
      Arguments.of("guest", Role.USER, false)
    );
  }
}
```

#### External MethodSource

@MethodSource 에서 외부 클래스의 인수 제공자를 제공하는 방법은 다음과 같다.

- @MethodSource("{classPath}#{methodName}")

```java
class JupiterParameterizedTest {
  @DisplayName("문자열 인수 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @MethodSource("com.gmoon.junit5.jupiter.argumentssource.provider.CommonArgumentsProvider#stringProvider")
  void methodSource1(String actual) {
    assertThat(actual).isNotNull();
  }

  @DisplayName("음수 인수 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @MethodSource("com.gmoon.junit5.jupiter.argumentssource.provider.CommonArgumentsProvider#negativeNumberProvider")
  void methodSource2(Integer actual) {
    assertThat(actual < 0).isTrue();
  }
}

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgumentsProvider {

  static Stream<String> stringProvider() {
    return Stream.of("gmoon", "guest");
  }

  static IntStream negativeNumberProvider() {
    return IntStream.range(-10, 0).skip(0);
  }
}
```

### ArgumentsAccessor

```java
class JupiterParameterizedTest {

  @DisplayName("ArgumentsAccessor 학습 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @MethodSource("userProvider")
  void methodSourceWithArgumentsAccessor(ArgumentsAccessor arguments) {
    String userName = arguments.getString(0);
    Role role = arguments.get(1, Role.class);
    boolean enabled = arguments.getBoolean(2);

    assertThat(userName).isNotNull();
    assertThat(role).isInstanceOf(Role.class);
    assertThat(enabled).isInstanceOf(Boolean.class);
  }

  static Stream<Arguments> userProvider() {
    return Stream.of(
      Arguments.of("gmoon", Role.ADMIN, true),
      Arguments.of("guest", Role.USER, false)
    );
  }
}
```

### @AggregateWith

````java
class JupiterParameterizedTest {

  @DisplayName("ArgumentsAccessor 학습 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @MethodSource("userProvider")
  void methodSourceWithArgumentsAccessor(@AggregateWith(MemberAggregator.class) Member member) {
    String userName = member.getName();
    Role role = member.getRole();
    boolean enabled = member.getEnabled();

    assertThat(userName).isNotNull();
    assertThat(role).isInstanceOf(Role.class);
    assertThat(enabled).isInstanceOf(Boolean.class);
  }
}

public class MemberAggregator implements ArgumentsAggregator {

  @Override
  public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
    throws ArgumentsAggregationException {
    Member member = new Member();
    member.setName(accessor.getString(0));
    member.setRole(accessor.get(1, Role.class));
    member.setEnabled(accessor.getBoolean(2));
    return member;
  }
}
````

### @ConvertWith

- ArgumentConverter
- @ConvertWith

```java
class JupiterParameterizedTest {

  @DisplayName("인수 변환 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @EnumSource(Role.class)
  void enumSourceWithConvert(@ConvertWith(ToStringArgumentConverter.class) String roleName) {
    assertThat(Role.valueOf(roleName))
      .isNotNull();
  }
}

public class ToStringArgumentConverter extends SimpleArgumentConverter {

  @Override
  protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
    assertEquals(String.class, targetType, "Can only convert to String");
    if (source instanceof Enum<?>) {
      return ( (Enum<?>) source ).name();
    }
    return String.valueOf(source);
  }
}
```

## Custom @ArgumentsSource

@ArgumentsSource, ArgumentsProvider 인터페이스를 구현한 한다.

- ParameterizedTestExtension#provideTestTemplateInvocationContexts(ExtensionContext extensionContext)
    - org.junit.jupiter.params.provider.Arguments
    - org.junit.jupiter.params.provider.ArgumentsProvider

```java
class JupiterParameterizedTest {

  @DisplayName("커스텀 음수 어노테이션 테스트")
  @ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
  @NegativeNumbers
  void customAnnotation(int actual) {
    assertThat(actual < 0).isTrue();
  }
}

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(NegativeNumbersProvider.class)
public @interface NegativeNumbers {

  int start() default -10;

  int end() default -1;
}

public class NegativeNumbersProvider implements ArgumentsProvider, AnnotationConsumer<NegativeNumbers> {

  private NegativeNumbers negativeNumbers;

  @Override
  public void accept(NegativeNumbers negativeNumbers) {
    this.negativeNumbers = negativeNumbers;
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    int startInclusive = negativeNumbers.start();
    int endInclusive = negativeNumbers.end();
    Preconditions.condition(isNegativeNumbers(startInclusive),
      String.format("The starting(%d) value must be negative.", startInclusive));
    Preconditions.condition(isNegativeNumbers(endInclusive),
      String.format("The end(%d) value must be negative.", endInclusive));
    Preconditions.condition(startInclusive < endInclusive, "start must be less than end.");

    return IntStream.rangeClosed(startInclusive, endInclusive)
      .mapToObj(Arguments::of);
  }

  private boolean isNegativeNumbers(int number) {
    return number < 0;
  }
}
```

## Reference

- [junit.org - parameterized](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests)
- [junit.org - tests sources](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests-sources)

## TODO

- Argument Providers and Sources
    - ArgumentsProvider
    - @ArgumentsSource
- Formal Parameter List
    - ParameterResolvers
    - ArgumentsAccessor
    - @AggregateWith.
- Argument Conversion
    - @ConvertWith
    - ArgumentConverter
- Composed Annotations
    - meta-annotation 의미를 지닌 커스텀 합성(composed) 어노테이션을 구현을 지원한다.
- Test Execution Order
    - @TestInstance(Lifecycle.PER_CLASS)
