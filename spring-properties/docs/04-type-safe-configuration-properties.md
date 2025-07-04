# Spring Boot 프로퍼티 설정

## [타입 세이프 설정 프로퍼티 (Type-safe Configuration Properties)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties)

`@Value("${property}")` 애노테이션을 사용해서 설정 프로퍼티를 주입하는 방법은, 여러 프로퍼티를 다뤄야 하거나 데이터가 계층적인 구조를 가지고 있을 때 다소 번거로울 수 있다. 

`Spring Boot`는 이런 상황에서 애플리케이션 설정을 더 안전하고 편리하게 관리할 수 있도록, 타입이 명확하게 지정된(type-safe) 빈(Bean)을 통해 설정을 관리하고 검증할 수 있는 별도의 방법을 제공한다.

> `@Value`와 type-safe 설정 프로퍼티의 차이점은 [해당 문서](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.vs-value-annotation) 에서 확인할 수 있다.

---

## [자바빈 프로퍼티 바인딩 (JavaBean Properties Binding)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.java-bean-binding)

표준 자바빈(JavaBean) 프로퍼티를 선언한 빈(Bean)에 값을 바인딩할 수 있다.

```java
@ConfigurationProperties("my.service")
public class MyProperties {

	private boolean enabled;
	private InetAddress remoteAddress;
	private final Security security = new Security();

	// getter / setter

	public static class Security {
		private String username;
		private String password;
		private List<String> roles = new ArrayList<>(Collections.singleton("USER"));

		// getter / setter
	}
}
```

위의 POJO는 다음과 같은 프로퍼티를 정의한다.

- `my.service.enabled` : 기본값은 false다.
- `my.service.remote-address` : String에서 변환될 수 있는 타입이다.
- `my.service.security.username` : "security"라는 이름의 중첩 객체 안에 있는 프로퍼티이며, 이 때 타입명은 사용되지 않고 이름만 중요하다(예를 들어 SecurityProperties라 해도 상관없다).
- `my.service.security.password`
- `my.service.security.roles` : 기본값이 "USER"인 String 컬렉션이다.

프로퍼티 이름에 예약어(예: `my.service.import`)를 사용하려면, 필드에 @Name 애노테이션을 사용하면 된다.

@ConfigurationProperties 클래스로 매핑되는 프로퍼티들은, 스프링 부트에서 프로퍼티 파일, YAML 파일, 환경 변수 등 여러 방식으로 설정할 수 있다. 이 클래스들은 public API이지만 클래스 내부의 `getter`/`setter` 메서드를 직접 사용하는 용도가 아니다.

이 구조는 기본 생성자가 필요하며, `getter`/`setter`가 필수적인 경우가 많다. 바인딩은 스프링 MVC처럼 표준 Java Beans 프로퍼티 디스크립터를 통해 이루어지기 때문이다.

### 단, 다음 경우에는 `setter` 생략이 가능하다.

- Map 타입: 초기화되어 있으면 `setter` 없이 바인딩 가능
- 컬렉션/배열: 
  - YAML 파일에서는 인덱스를 통한 바인딩이 가능, `setter` 생략 가능
  - 프로퍼티 파일에서는 구분 값으로 콤마 사용, `setter` 필수.
  - 컬렉션을 초기화할 때에는 불변 객체 사용 불가
  - 바인딩 방식(YAML이든 프로퍼티 파일이든)을 고려할 필요 없이, 컬렉션 타입에는 항상 `setter`를 추가하는 것이 좋다. 
- 중첩된 POJO: 필드에서 직접 초기화되어 있다면 `setter` 생략 가능 
  - 예: `Security` 필드
  - 단 바인더가 기본 생성자를 사용해서 인스턴스를 생성해야 한다면 `setter가` 필요하다.

`Lombok`을 사용해 `getter`/`setter`를 자동으로 추가한다.
이 때 `Lombok`이 특별한 생성자를 생성하지 않는지 확인해야 한다. 컨테이너가 객체 생성에 **기본 생성자를 자동으로 사용하기 때문이다.**

마지막으로, 표준 Java Bean 프로퍼티만 바인딩에 고려되며, **static 프로퍼티 바인딩은 지원하지 않는다.**

---

## [생성자 바인딩 (Constructor Binding)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.constructor-binding)

이전 섹션의 예시는, 아래와 같이 변경하면 불변(immutable) 객체 형태로 작성할 수 있다.

```java
@ConfigurationProperties("my.service")
@RequiredArgsConstructor
@Getter
public class MyProperties {

	private final boolean enabled;
	private final InetAddress remoteAddress;
	private final Security security;

	@Getter
	public static class Security {
		private final String username;
		private final String password;
		private final List<String> roles;

		public Security(String username, String password, @DefaultValue("USER") List<String> roles) {
			this.username = username;
			this.password = password;
			this.roles = roles;
		}
	}
}
```

이 구조에서 단 하나의 생성자만 존재하므로 Spring은 생성자 바인딩을 자동 적용한다.

생성자가 둘 이상인 경우, 바인딩에 사용할 생성자에 `@ConstructorBinding` 애노테이션을 명시한다.

### 생성자 바인딩을 비활성화하려면

```java
@ConfigurationProperties("my")
@Getter
@Setter
public class MyProperties {

	final MyBean myBean;
	private String name;

	@Autowired
	public MyProperties(MyBean myBean) {
		this.myBean = myBean;
	}
}
```

- 해당 생성자에 `@Autowired` 선언
- 생성자의 접근제한자 `private` 명시
- Kotlin에서는 기본 생성자를 구성

### 레코드 타입 지원

생성자 바인딩은 record 타입에서도 사용할 수 있으며, 생성자가 하나뿐이라면 `@ConstructorBinding` 애노테이션은 생략해도 된다.

### @DefaultValue

생성자 파라미터나 레코드 컴포넌트에 `@DefaultValue`를 붙이면, 기본값을 지정할 수 있다. 변환 서비스가 동작하여, 프로퍼티가 없는 경우에 이 String 값을 타입에 맞게 변환해준다.

중첩 클래스도 생성자 바인딩이 적용된다. 필수 필드가 없어도 객체 자체를 생성하고 싶다면 `@DefaultValue` 애노테이션을 빈 문자열(`""`)로 지정하면 된다.

```java
public MyProperties(@DefaultValue Security security) {
}
```

- Security에 바인딩되는 프로퍼티가 없으면 MyProperties의 security 값은 null이 된다. 
- 만약 프로퍼티가 바인딩되지 않아도 security 값이 null이 아닌 인스턴스가 되게 하려면(코틀린에서는 Security의 username, password 파라미터를 nullable로 만들어야 한다), 아래처럼 빈 @DefaultValue 애노테이션을 사용하면 된다.

### 생성자 바인딩 활성화

생성자 바인딩을 사용하려면 해당 클래스에 대해 `@EnableConfigurationProperties` 또는 `@ConfigurationPropertiesScan` 설정이 필요하다. 

생성자 바인딩은 `@Component` 애노테이션으로 생성되거나, `@Bean` 메서드, `@Import`로 로드되는 일반적인 방식으로 생성되는 빈에서는 사용할 수 없다.

또한 생성자 바인딩을 사용하려면, 코드를 `-parameters` 옵션을 붙여 컴파일해야 한다. Spring Boot의 Gradle 플러그인을 사용하거나, Maven에서 `spring-boot-starter-parent`를 사용하면 이 옵션이 자동으로 적용된다.

`@ConfigurationProperties`에서 Optional 타입은 주로 반환 타입으로 설계되었으므로, `Optional`은 설정 바인딩에 적합하지 않으며, 다른 타입과의 일관성 측면에서도 바인딩 시 값이 없으면 `Optional.empty()`가 아닌 `null`로 바인딩 된다.

프로퍼티 이름에 예약어(예: my.service.import)를 사용하고 싶다면, 생성자 파라미터에 `@Name` 애노테이션을 사용하면 된다.

---

## [@ConfigurationProperties 애노테이션 타입 활성화](#https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.enabling-annotated-types)

Spring Boot는 `@ConfigurationProperties`로 선언된 타입을 바인딩해서 빈으로 등록할 수 있는 인프라를 제공한다. 이 기능은 클래스 단위로 명시하거나, 혹은 컴포넌트 스캔과 비슷하게 동작하는 '프로퍼티 스캔'을 활성화해서 사용할 수도 있다.

### @EnableConfigurationProperties: 스캔 방식이 적절하지 않은 경우 

스캔 방식이 적절하지 않은 경우 `@EnableConfigurationProperties` 애노테이션을 사용해서, 직접 처리할 타입 목록을 지정할 수 있다.

- auto-configuration 개발
- 조건부 활성화

```java
// 이 애노테이션은 어떤 @Configuration 클래스에서든 사용할 수 있다.
@Configuration
@EnableConfigurationProperties(SomeProperties.class)
public class MyConfiguration {
}

@ConfigurationProperties("some.properties")
public class SomeProperties {
}
```

### @ConfigurationPropertiesScan: 프로퍼티 스캔 

프로퍼티 스캔을 사용할 경우, `@ConfigurationPropertiesScan` 애노테이션을 사용하면 된다. 기본적으로 선언된 클래스의 패키지부터 스캔되며, 명시적으로 지정할 수도 있다.

- `@SpringBootApplication` 메인 클래스
- `@Configuration` 클래스

```java
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan({ "com.example.app", "com.example.another" })
public class MyApplication {

}
```

`@ConfigurationProperties` 빈이 프로퍼티 스캔이나 `@EnableConfigurationProperties`를 통해 등록되면, 해당 빈의 이름은 `<prefix>-<fqn>` 이름으로 등록된다.

- prefix: 애노테이션에서 정의한 값
- fqn: 클래스의 FQCN

```java
package com.gmoon.app;

@ConfigurationProperties(prefix = "some")
public class SomeProperties {
}
```

- `some`-`com.gmoon.app.SomeProperties`

만약 `prefix` 없이 선언되었다면, 빈 이름은 클래스의 전체 경로명만 사용된다.

`@ConfigurationProperties` 클래스에서는 **환경설정 바인딩에만 집중하는 것을 권장한다.**

### 생성자 빈 주입을 피해라.

다른 빈 주입이 필요하다면?

- 세터 주입(setter injection)을 사용하거나, 
- 프레임워크가 제공하는 Aware 인터페이스를 사용한다.
  - 예: `Environment`에 접근이 필요한 경우 `EnvironmentAware`를 사용한다.

어쩔 수 없이 생성자 주입이 필요하다면, 

해당 프로퍼티 클래스에 `@Component`를 선언하고, JavaBean 방식의 프로퍼티 바인딩(getter/setter 를 이용하는 방식)을 사용해야 합니다. 

**프로퍼티 클래스에서 "생성자 빈 주입" 방식은 불변 객체를 불가능하게 만든다.**

---

## [@ConfigurationProperties 사용 예시](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.using-annotated-types)

이 스타일은 `SpringApplication`의 외부 YAML 설정과 함께 사용할 때 특히 유용하다.

```yaml
my:
  service:
    remote-address: 192.168.1.1
    security:
      username: "admin"
      roles:
        - "USER"
        - "ADMIN"
```

`@ConfigurationProperties`로 선언된 빈은 다른 빈과 마찬가지로 의존성 주입을 통해 사용할 수 있다.

```java
@Service
public class MyService {

	private final MyProperties properties;

	public MyService(MyProperties properties) {
		this.properties = properties;
	}

	public void openConnection() {
		Server server = new Server(properties.getRemoteAddress());
		server.start();
	}
}
```

`@ConfigurationProperties`를 사용하면, `IDE`에서 자동완성을 제공할 수 있는 [메타데이터 파일](https://docs.spring.io/spring-boot/specification/configuration-metadata/index.html)을 생성할 수 있다.

---

## [서드파티(Third-party) 설정](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.third-party-configuration)

클래스에 `@ConfigurationProperties`를 붙이는 것뿐만 아니라, 공개된 `@Bean` 메서드에도 붙일 수 있다. 이 방식은 특히 내 제어권 밖에 있는 서드파티 컴포넌트에 프로퍼티를 바인딩할 때 유용하다.

`Environment` 프로퍼티를 통해 빈을 설정하려면, 아래 예시처럼 해당 빈을 등록할 때 `@ConfigurationProperties`를 같이 붙이면 된다.

```java
@Configuration(proxyBeanMethods = false)
public class ThirdPartyConfiguration {

	@Bean
	@ConfigurationProperties("another")
	public AnotherComponent anotherComponent() {
		return new AnotherComponent();
	}
}
```

`another` 접두사(prefix)를 갖는 모든 JavaBean 프로퍼티가 `AnotherComponent` 빈에 매핑된다.

---

이 문서는 Spring Boot 설정 파일의 유연한 구성과 다양한 환경 대응 방식을 설명하며, 특히 클라우드 환경에서의 운영에 유용한 정보를 포함하고 있다.

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
