# Spring boot properties

### [01. 우선순위가 가장 높은 설정 파일 값으로 적용된다.](00-spring-boot-property-loading-and-priority.md)

- 클래스 패스 루트
- 클래스 패스의 `/config` 디렉토리
- 현재 디렉토리 기준으로 반복
- 위에서 오른쪽, 왼쪽에서 오른쪽으로 우선순위가 높다.

### [설정 파일 확장](01-external-config.md)

- `spring.config.location`
- `spring.config.additional-location`
- `spring.config.import`
    - 현재 설정 파일보다 우선순위가 높다.
    - 설정 파일 문서 내에서 import 순서는 중요하지 않다.
    - 선언한 설정 파일 아래 추가적인 설정 문서(document)로 추가된다.
    - 중복 로딩되지 않는다.
    - 리스트 또는 배열은 마지막에 적용된 설정 파일의 값으로 적용된다.

### 다중 설정 파일 설정

- [프로파일 - spring.profiles.active](02-profile-specific-files.md)
- `#---`, `---` 도큐먼트를 구분한다. 참고. [03-spring-config-import.md](03-spring-config-import.md)

### [타입 세이프 설정 파일](04-type-safe-configuration-properties.md)

- @ConfigurationProperties
    - @ConfigurationPropertiesScan: 프로퍼티 스캔 방식
    - @EnableConfigurationProperties: 직접 지정
        - auto-configuration 개발
        - 조건부 활성화
- 불변 객체 만들기.
    - 클래스 변수에 직접 인스턴스가 초기화한 경우
        - Collections 객체 setter 생략 가능
    - 생성자 바인딩
        - 단 하나의 생성자만 존재한 경우
        - 둘 이상인 경우엔 `@ConstructorBinding` 명시
    - 레코드 타입 지원
        - `@ConstructorBinding` 생략 가능
- YAML 프로퍼티 파일은 SpEL 문법을 미지원한다.

### [느슨한 바인딩 전략(Relaxed binding)](05-relaxed-binding.md)

- 프로퍼티 속성 이름은 영문 소문자 형태의 케밥 케이스 스타일로 작성해라.
    - 알파벳 소문자
    - 숫자
    - 하이픈(-)
    - 점(.)
- 환경 변수를 자동 매핑해준다.
- 프로퍼티 클래스가 불변 객체라면 캐싱처리되어 동작된다.

### [프로퍼티 변환](06-property-conversion.md)

- `@ConfigurationProperties`가 붙은 빈에 외부 설정값을 바인딩할 때, 자동으로 타입을 맞춰주려고 시도한다.
- 커스텀 타입 바인딩은 `ConversionService` 활용하여 등록해라.
    - 빈 등록시 `static` 메서드를 활용해 어플리케이션 라이프사이클 초기에 등록되도록 권장한다.
    - 설정 클래스 내에 다른 빈 의존성은 최소화 해라.
- Duration
    - 기본 단위는 밀리초(ms)이지만, 클래스에서 `@DurationUnit`으로 바꿔줄 수 있다.
    - `ns` : 나노초
    - `us` : 마이크로초
    - `ms` : 밀리초
    - `s` : 초
    - `m` : 분
    - `h` : 시간
    - `d` : 일
    - 만약 예전 코드에서 `Long` 타입 property로 시간을 표현하고 있었다면, 단위가 밀리초가 아니라면 꼭 `@DurationUnit`으로 단위를 명확히 명시하는 것이 좋다. 이렇게 하면
      호환성을 유지하면서 더 다양한 값 형식을 지원할 수 있다.
- Period
    - 정수(int) 값 하나만 쓰면 기본 단위는 일(day)이다. 단, `@PeriodUnit`으로 바꿀 수도 있다.
    - `y` : 연
    - `m` : 월
    - `w` : 주
    - `d` : 일
    - 참고로, Period 타입 내부적으로는 ‘주(weeks)’ 단위가 따로 저장되지 않고 항상 7일로 변환되어 처리된다. 즉, 1주라고 입력해도 실제로는 7일로 계산된다.
- DataSize
    - Spring엔 데이터를 바이트 단위로 표현할 수 있는 `DataSize` 타입이 있다.
    - 숫자(long) 값만 쓸 때(별도의 단위 지정 없이): 기본 단위는 바이트(byte)다. 만약 `@DataSizeUnit` 어노테이션을 사용하면, 기본 단위를 바꿀 수 있다.
    - B  : 바이트
    - KB : 킬로바이트
    - MB : 메가바이트
    - GB : 기가바이트
    - TB : 테라바이트
- Base64
    - Spring Boot는 Base64로 인코딩된 이진 데이터를 설정값으로 사용할 수 있도록 지원한다.
    ```properties
    my.property=base64:SGVsbG8gV29ybGQ=
    ```
    - (Resource 프로퍼티로 파일, URL 등 다양한 리소스를 쓸 수도 있다.  
      예시는 여기서 잘렸지만, 일반적으로 base64 외에도 file:, http: 같은 접두사도 지원한다.)

### @ConfigurationProperties 검증(Validation)

- 검증에는 JSR-303 표준의 jakarta.validation 제약(Constraint) 어노테이션을 필드에 직접 사용할 수 있다. 이 기능을 쓰려면, `JSR-303`을 지원하는 라이브러리가
  클래스패스에 추가돼 있어야 한다.
- 추가로, 커스텀 `Spring Validator`를 쓰고 싶을 땐, 빈 이름을 `configurationPropertiesValidator`로 해서 빈을 등록하면 된다.
- 이때 `@Bean` 메서드는 반드시 `static`이어야 한다.

### @ConfigurationProperties VS @Value

| 특징                  | @ConfigurationProperties | @Value        |
|---------------------|--------------------------|---------------|
| Relaxed binding     | 지원                       | 제한적 지원(아래 참고) |
| 메타데이터 지원            | 지원                       | 미지원           |
| SpEL(Expression) 지원 | 미지원                      | 지원            |

프로퍼티 파일은 SpEL 을 미지원한다.

```properties
a=1
b=2
result=#{a+b}
```

예를 들어, 

- 프로퍼티 파일은 문자열 그대로 `#{a+b}` 로 인식된다.
- YAML 파일은 SpEL의 잘못된 값(문자열 인식 불가)이므로, 빈 문자열 값("")으로 바인딩된다.
- 둘 모두 `@Value`로 읽을 때만 SpEL 평가(계산)됨
