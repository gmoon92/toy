# Spring Boot 프로퍼티 설정

## [프로퍼티 변환(Properties Conversion)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion)

Spring Boot는 `@ConfigurationProperties`가 붙은 빈에 외부 설정값을 바인딩할 때, 자동으로 타입을 맞춰주려고 시도한다.  

만약 직접 커스텀 타입 변환이 필요하다면, 다음과 같은 방법을 활용할 수 있다.

- `ConversionService` 빈(이름이 **conversionService**로 등록된 것)을 직접 제공하기
- `CustomEditorConfigurer` 빈을 통해 커스텀 프로퍼티 에디터 등록하기
- `@ConfigurationPropertiesBinding`이 붙은 빈으로 커스텀 컨버터 정의하기

설정 값을 변환해주는 빈(`ConversionService`나 컨버터 등)은 어플리케이션 라이프사이클 초기에 굉장히 일찍 생성된다.  

따라서, 이런 빈에서 사용하는 의존성들은 최소화하는 게 좋다.  
왜냐하면, 이 시점에는 필요한 다른 빈들이 아직 완전히 초기화되지 않은 경우가 많기 때문이다.

만약 커스텀 `ConversionService`가 프로퍼티 변환 용도가 아니라면(즉, 설정값 바인딩이 아니라 다른 데에서만 쓰고 싶다면),  
ConversionService 빈 이름을 바꿔 등록하는 것이 좋다.  
프로퍼티 바인딩에만 필요한 커스텀 변환이 있다면, `@ConfigurationPropertiesBinding`이 붙은 컨버터만 등록해서 써도 충분하다.

참고로, `@ConfigurationPropertiesBinding`이 붙은 커스텀 컨버터를 빈으로 만들 때는  
@Bean 메서드를 `static`으로 작성하는 것이 좋다.  

그래야 "빈이 모든 BeanPostProcessor로부터 처리가 불가능하다"는 경고 메시지가 뜨지 않는다.

### [Duration(기간) 변환](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.durations)

`Spring Boot`에서는 시간(Duration)을 표현하는 여러 방식들을 지원한다.  
만약 configuration 속성으로 `Duration` 타입을 쓴다면, application 프로퍼티 파일에서는 아래와 같은 다양한 방식으로 값을 적을 수 있다.

- 숫자(long)로 입력 (기본 단위는 밀리초, 단 `@DurationUnit` 어노테이션을 사용하면 기본 단위를 바꿀 수 있다)
- `Duration`에서 사용하는 표준 ISO-8601 형식(예: `PT30S`)
- 값과 단위를 붙여 쓴 읽기 쉬운 형식(예: `10s`는 10초라는 의미)

```java
@ConfigurationProperties("my")
public class MyProperties {
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration sessionTimeout = Duration.ofSeconds(30);

    private Duration readTimeout = Duration.ofMillis(1000);

    // ...getter, setter 생략
}
```

여기에서 `sessionTimeout`을 30초로 지정하려면, 프로퍼티에서 `30`, `PT30S`, `30s`와 같이 다양한 방식으로 값을 쓸 수 있다. `readTimeout`을 500ms로 지정할 때도 마찬가지로, `500`, `PT0.5S`, `500ms` 모두 사용할 수 있다.

사용할 수 있는 단위는 아래와 같다.

- `ns` : 나노초
- `us` : 마이크로초
- `ms` : 밀리초
- `s` : 초
- `m` : 분
- `h` : 시간
- `d` : 일

기본 단위는 밀리초(ms)이지만, 클래스에서 `@DurationUnit`으로 바꿔줄 수 있다.

생성자 바인딩(Constructor Binding)을 쓰는 경우에도 동일한 방식으로 값을 받을 수 있다.

```java
@ConfigurationProperties("my")
public class MyProperties {
    private final Duration sessionTimeout;
    private final Duration readTimeout;

    public MyProperties(
        @DurationUnit(ChronoUnit.SECONDS) @DefaultValue("30s") Duration sessionTimeout,
        @DefaultValue("1000ms") Duration readTimeout
    ) {
        this.sessionTimeout = sessionTimeout;
        this.readTimeout = readTimeout;
    }

    // ...getter 생략
}
```

만약 예전 코드에서 `Long` 타입 property로 시간을 표현하고 있었다면, 단위가 밀리초가 아니라면 꼭 `@DurationUnit`으로 단위를 명확히 명시하는 것이 좋다.  
이렇게 하면 호환성을 유지하면서 더 다양한 값 형식을 지원할 수 있다.

### [Period(기간/주기) 변환](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.periods)

Duration과 더불어, Spring Boot는 `Period` 타입도 지원한다.  
Period 타입은 주로 "X년 Y개월 Z일" 같은 값을 나타내는데, application 프로퍼티에서는 다음과 같은 형식으로 사용 가능하다.

- 정수(int) 값 하나만 쓰면 기본 단위는 일(day)이다. 단, `@PeriodUnit`으로 바꿀 수도 있다.
- ISO-8601 표준 형식(예: `P1Y3D` 등)
- 값과 단위를 합쳐서 쓴 간단한 형식(예: `1y3d`는 1년 3일)

간단한 형식에서 사용할 수 있는 단위는 아래와 같다.

- `y` : 연
- `m` : 월
- `w` : 주
- `d` : 일

참고로, Period 타입 내부적으로는 ‘주(weeks)’ 단위가 따로 저장되지 않고 항상 7일로 변환되어 처리된다.  
즉, 1주라고 입력해도 실제로는 7일로 계산된다.

### [데이터 크기(DataSize) 변환](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.data-sizes)

Spring Framework에는 데이터를 바이트 단위로 표현할 수 있는 `DataSize` 타입이 있다.  
`DataSize` 타입의 프로퍼티를 만들면, application 프로퍼티 파일에서 다음과 같은 형식으로 값을 지정할 수 있다.

- 숫자(long) 값만 쓸 때(별도의 단위 지정 없이): 기본 단위는 바이트(byte)다. 만약 `@DataSizeUnit` 어노테이션을 사용하면, 기본 단위를 바꿀 수 있다.
- 더 읽기 쉬운 형식으로, 값과 단위를 함께 적을 수 있다. (예: `10MB`는 10메가바이트를 의미한다)

예를 들면, 아래와 같은 코드가 있다고 하자.

```java
@ConfigurationProperties("my")
public class MyProperties {

    @DataSizeUnit(DataUnit.MEGABYTES)
    private DataSize bufferSize = DataSize.ofMegabytes(2);

    private DataSize sizeThreshold = DataSize.ofBytes(512);

    // getter, setter 생략
}
```

여기서 버퍼 크기(`bufferSize`)를 10메가바이트로 주고 싶다면, 프로퍼티에서 `10`이나 `10MB` 둘 다 사용 가능하다.  
임계값(`sizeThreshold`)을 256바이트로 지정하려면, `256` 또는 `256B`로 적으면 된다.

지원하는 단위는 아래와 같다.

- B  : 바이트
- KB : 킬로바이트
- MB : 메가바이트
- GB : 기가바이트
- TB : 테라바이트

기본 단위는 바이트지만, 위의 코드처럼 `@DataSizeUnit` 어노테이션으로 원하는 단위로 바꿀 수 있다.

만약 생성자 바인딩을 선호한다면, 다음과 같이 작성할 수도 있다.

```java
@ConfigurationProperties("my")
public class MyProperties {
    private final DataSize bufferSize;
    private final DataSize sizeThreshold;

    public MyProperties(
        @DataSizeUnit(DataUnit.MEGABYTES) @DefaultValue("2MB") DataSize bufferSize,
        @DefaultValue("512B") DataSize sizeThreshold
    ) {
        this.bufferSize = bufferSize;
        this.sizeThreshold = sizeThreshold;
    }

    // getter 생략
}
```

기존에 long 타입 프로퍼티를 쓰고 있었다면, 단위가 바이트가 아니면 `@DataSizeUnit`을 꼭 지정해주는 게 좋다.  
이렇게 하면, 이전 값은 호환성을 유지하면서 더 다양한 형식으로 데이터를 지정할 수 있다.

### [Base64 데이터 변환](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.base64)

Spring Boot는 Base64로 인코딩된 이진 데이터를 설정값으로 사용할 수 있도록 지원한다.  
만약 `Resource` 타입의 프로퍼티를 만들었다면, 아래 예시처럼 프로퍼티 값에 `base64:` 접두사를 붙여 Base64 문자열을 바로 지정할 수 있다.

```properties
my.property=base64:SGVsbG8gV29ybGQ=
```

위와 같이 값을 입력하면, 해당 문자열이 Base64로 디코딩되어 Resource 타입에 바인딩된다.

(Resource 프로퍼티로 파일, URL 등 다양한 리소스를 쓸 수도 있다.  
예시는 여기서 잘렸지만, 일반적으로 base64 외에도 file:, http: 같은 접두사도 지원한다.)

---

### [@ConfigurationProperties 검증(Validation)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.validation)

Spring Boot에서는 `@ConfigurationProperties` 클래스를 만들 때, 클래스에 Spring의 `@Validated` 어노테이션을 붙이면 자동으로 값 검증이 동작한다.

검증에는 JSR-303 표준의 jakarta.validation 제약(Constraint) 어노테이션을 필드에 직접 사용할 수 있다. 이 기능을 쓰려면, `JSR-303`을 지원하는 라이브러리가 클래스패스에 추가돼 있어야 한다.

```java
@ConfigurationProperties("my.service")
@Validated
public class MyProperties {
    @NotNull
    private InetAddress remoteAddress;

    // getter, setter 생략
}
```

`@ConfigurationProperties` 를 생성해주는 `@Bean` 메서드에 `@Validated`를 붙여서도 검증을 동작시킬 수 있다.

만약 속성 클래스 안에 중첩 객체가 있고, 그 안의 값도 함께 검증하고 싶다면 해당 필드에 `@Valid`를 붙이면 된다. 아래는 앞의 예제에 보안 관련 중첩 클래스를 추가한 예제다.

```java
@ConfigurationProperties("my.service")
@Validated
public class MyProperties {

    @NotNull
    private InetAddress remoteAddress;

    @Valid
    private final Security security = new Security();

    // getter, setter 생략

    public static class Security {
        @NotEmpty
        private String username;

        // getter, setter 생략
    }
}
```

추가로, 커스텀 `Spring Validator`를 쓰고 싶을 땐, 빈 이름을 `configurationPropertiesValidator`로 해서 빈을 등록하면 된다.

- 이때 `@Bean` 메서드는 반드시 `static`이어야 한다.

`Configuration Properties Validator`는 애플리케이션 라이프사이클에서 상당히 초기 단계에 생성되기 때문에, `static` 메서드로 선언하면 `@Configuration` 클래스 인스턴스가 미리 만들어지지 않아도 Bean을 생성할 수 있다.

이렇게 하면 초기화가 너무 빨리 발생해 생기는 문제를 피할 수 있다.

> `spring-boot-actuator` 모듈을 사용하면 `/actuator/configprops` 엔드포인트(또는 JMX 엔드포인트)를 통해 등록된 모든 `@ConfigurationProperties` 빈의 정보를 한눈에 볼 수 있다.<br/>
> 자세한 내용은 [Production ready features 섹션](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)을 참고하면 된다.

---

### [@ConfigurationProperties와 @Value의 차이점](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.vs-value-annotation)

`@Value` 어노테이션은 Spring의 핵심 기능이지만,

타입 안전한 타입-세이프(type-safe) 환경설정(`@ConfigurationProperties`)이 가진 기능들을 모두 제공하는 것은 아니다.

아래 표는 `@ConfigurationProperties`와 `@Value`가 각각 지원하는 기능들을 비교한 것이다.

| 특징                  | @ConfigurationProperties | @Value        |
|---------------------|--------------------------|---------------|
| Relaxed binding     | 지원                       | 제한적 지원(아래 참고) |
| 메타데이터 지원            | 지원                       | 미지원           |
| SpEL(Expression) 지원 | 미지원                      | 지원            |


#### Relaxed Binding

- `@ConfigurationProperties`는 relaxed binding(소문자, 하이픈, 카멜케이스 등 다양한 형태의 이름 자동 매칭)을 완전히 지원한다.
- `@Value`도 어느 정도는 지원하지만, 사용법에 따라 제한이 있다.

#### 그 외 지원

- `@ConfigurationProperties`는 type-safe 객체를 쓸 수 있고, 메타데이터도 생성된다.
- `@Value`는 SpEL(Spring Expression Language) 식을 쓸 수 있다.

---

## 프로퍼티 이름은 케밥 케이스로 작성하라

**@Value 사용 시에는** 프로퍼티 이름을 될 수 있으면 표준형(canonical form, 예를 들어 소문자와 케밥 케이스, 즉 `demo.item-price`)로 적는 것이 좋다.

이렇게 하면, `@ConfigurationProperties`의 `relaxed binding`과 유사하게 동작하게 된다.

예를 들어,

```java
@Value("${demo.item-price}")
```

를 쓰면, `application.properties`에서 `demo.item-price`나 `demo.itemPrice`, 환경변수에서는 `DEMO_ITEMPRICE`로 등록된 값들도 모두 인식한다.

반면
```java
@Value("${demo.itemPrice}")
```

처럼 카멜케이스로 쓰면, 해당 환경설정 값이 정확하게 `demo.itemPrice`인 경우에만 읽을 수 있고, 하이픈이나 대문자 환경변수는 못 읽는다.

**커스텀 환경설정 키를 여러 개 정의할 때는**,  
별도의 POJO에 `@ConfigurationProperties`를 붙여서 묶는 걸 추천한다.  
이 방식이 타입 안정성, 구조화 등 여러 면에서 편리하다.

**참고로,** application property 파일에서 SpEL 감싼 값들이 즉시 처리되는 것이 아니라,  
`@Value`로 값을 주입할 때 비로소 SpEL이 실행된다.

즉, 스프링 환경이 프로퍼티 파일을 해석하며 SpEL을 공식적으로 지원하는 것은 아니고  
`@Value`로 주입받을 때에만 식이 평가된다는 점을 알아두면 된다.

---

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
