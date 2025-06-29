## [타입 세이프 설정 프로퍼티 (Type-safe Configuration Properties)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties)

`@Value("${property}")` 애노테이션을 사용하여 설정 프로퍼티를 주입하는 방식은 때때로 번거로울 수 있습니다. 특히 여러 개의 프로퍼티를 다루거나 계층적인 데이터를 다루는 경우 그렇습니다.
Spring Boot는 애플리케이션 설정을 강하게 타입화된 빈(bean)으로 관리하고 검증할 수 있도록 대안적인 방식을 제공합니다.

`@Value`와 타입 세이프 설정 프로퍼티 간의
차이점은 [해당 문서](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config.typesafe-configuration-properties.vs-value)
에서 확인할 수 있습니다.

---

## 자바빈 프로퍼티 바인딩 (JavaBean Properties Binding)

표준 JavaBean 프로퍼티를 선언하는 빈에 설정을 바인딩할 수 있습니다. 다음은 그 예시입니다:

```java
@ConfigurationProperties("my.service")
public class MyProperties {

	private boolean enabled;
	private InetAddress remoteAddress;
	private final Security security = new Security();

	// getter / setter 생략

	public static class Security {
		private String username;
		private String password;
		private List<String> roles = new ArrayList<>(Collections.singleton("USER"));

		// getter / setter 생략
	}
}
```

위 POJO는 다음과 같은 프로퍼티들을 정의합니다:

* `my.service.enabled` — 기본값: `false`
* `my.service.remote-address` — 문자열에서 `InetAddress`로 변환 가능
* `my.service.security.username` — 중첩된 `"security"` 객체의 속성으로 바인딩됨 (클래스명이 아닌 필드명이 기준)
* `my.service.security.password`
* `my.service.security.roles` — 기본값: `"USER"` 하나가 담긴 리스트

프로퍼티 이름에 예약어(`import` 등)를 사용할 경우, 필드에 `@Name` 애노테이션을 붙이면 됩니다.

Spring Boot에서 `@ConfigurationProperties`로 매핑되는 클래스는 프로퍼티 파일, YAML, 환경 변수 등을 통해 설정되며, 해당 클래스의 게터/세터는 바인딩 목적으로만 사용되고 직접
접근하는 것은 권장되지 않습니다.

JavaBeans 방식이므로 기본 생성자와 게터/세터가 필요합니다. 단, 다음 경우에는 세터 생략이 가능합니다:

* Map 타입: 초기화되어 있으면 세터 없이 바인딩 가능
* 컬렉션/배열: YAML에서는 인덱스, 프로퍼티에서는 쉼표 구분 문자열 사용. 후자의 경우 세터 필수.
* 중첩된 POJO: 필드에서 직접 초기화되어 있다면 세터 불필요 (예: `Security`)

Lombok을 사용하는 경우, 생성자를 자동 생성하면 컨테이너가 객체를 인스턴스화할 수 없으므로 Lombok 설정에 주의해야 합니다.

정적(static) 필드는 바인딩되지 않습니다.

---

## 생성자 바인딩 (Constructor Binding)

이전 예제를 불변 객체 스타일로 다시 작성하면 다음과 같습니다:

```java
@ConfigurationProperties("my.service")
public class MyProperties {

	private final boolean enabled;
	private final InetAddress remoteAddress;
	private final Security security;

	public MyProperties(boolean enabled, InetAddress remoteAddress, Security security) {
		this.enabled = enabled;
		this.remoteAddress = remoteAddress;
		this.security = security;
	}

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

이 구조에서 단 하나의 생성자만 존재하므로 Spring은 생성자 바인딩을 자동 적용합니다. 생성자가 둘 이상인 경우, 바인딩에 사용할 생성자에 `@ConstructorBinding` 애노테이션을 명시해야 합니다.

생성자 바인딩을 비활성화하려면:

* 해당 생성자에 `@Autowired`를 붙이거나
* 생성자를 `private`으로 만들면 됩니다.

Kotlin에서는 주 생성자를 비워두면 바인딩 대상에서 제외됩니다.

예:

```java
@ConfigurationProperties("my")
public class MyProperties {

	final MyBean myBean;
	private String name;

	@Autowired
	public MyProperties(MyBean myBean) {
		this.myBean = myBean;
	}

	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }
}
```

생성자 바인딩은 record 타입에서도 사용할 수 있으며, 생성자가 하나뿐이라면 `@ConstructorBinding` 애노테이션은 생략해도 됩니다.

중첩 클래스도 생성자 바인딩이 적용됩니다. 필수 필드가 없어도 객체 자체를 생성하고 싶다면 `@DefaultValue` 애노테이션을 빈 문자열(`""`)로 지정하면 됩니다:

```java
public MyProperties(boolean enabled, InetAddress remoteAddress, @DefaultValue Security security) {
	...
}
```

**주의:** 생성자 바인딩을 사용하려면 해당 클래스에 대해 `@EnableConfigurationProperties` 또는 `@ConfigurationPropertiesScan` 설정이 필요합니다. 일반적인
방식으로 생성되는 빈(`@Component`, `@Bean`, `@Import`)에서는 생성자 바인딩을 사용할 수 없습니다.

그리고 컴파일 시 `-parameters` 플래그가 필요하며, 이는 Spring Boot의 Gradle 플러그인 또는 `spring-boot-starter-parent`를 사용하는 Maven 설정에서는 자동으로
설정됩니다.

`Optional`은 설정 바인딩에 적합하지 않으며, 바인딩 시 값이 없으면 `Optional.empty()`가 아닌 `null`로 처리됩니다.

프로퍼티 이름에 예약어를 쓰려면 생성자 파라미터에 `@Name`을 붙입니다.

---

## @ConfigurationProperties 애노테이션 타입 활성화

Spring Boot는 `@ConfigurationProperties` 타입을 빈으로 등록하고 설정 값으로 바인딩하는 인프라를 제공합니다. 클래스 단위로 명시하거나, 컴포넌트 스캔과 유사한 설정 프로퍼티 스캔 방식도
사용할 수 있습니다.

스캔 방식이 적절하지 않은 경우 (예: 오토 설정, 조건부 등록), 특정 타입을 `@EnableConfigurationProperties`를 통해 명시합니다:

```java
@Configuration
@EnableConfigurationProperties(SomeProperties.class)
public class MyConfiguration {
}
```

```java

@ConfigurationProperties("some.properties")
public class SomeProperties {
}
```

설정 프로퍼티 스캔을 사용할 경우, `@ConfigurationPropertiesScan` 애노테이션을 메인 클래스나 `@Configuration` 클래스에 붙입니다. 기본적으로 현재 클래스의 패키지부터 스캔되며,
명시적으로 지정할 수도 있습니다:

```java
@SpringBootApplication
@ConfigurationPropertiesScan({ "com.example.app", "com.example.another" })
public class MyApplication {
}
```

이 방식으로 등록된 빈은 `<prefix>-<fqn>` 형식의 이름을 갖습니다. (`prefix`는 애노테이션에서 정의한 값, `fqn`은 클래스의 FQCN)

예를 들어 `com.example.app.SomeProperties` 클래스가 `some.properties`라는 prefix를 갖는다면, 빈 이름은
`some.properties-com.example.app.SomeProperties`입니다.

설정 클래스에서는 외부 설정만 다루는 것을 권장합니다. 다른 빈 주입이 필요할 경우 세터 주입이나 `EnvironmentAware` 등 Aware 인터페이스를 사용하는 것이 좋습니다. 어쩔 수 없이 생성자 주입이
필요하다면, 해당 클래스에 `@Component`를 붙이고 JavaBean 방식 바인딩을 사용해야 합니다.

---

## @ConfigurationProperties 사용 예시

이 스타일은 SpringApplication의 외부 YAML 설정과 함께 사용할 때 특히 유용합니다:

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

바인딩된 빈은 일반 빈처럼 의존성 주입을 통해 사용할 수 있습니다:

```java
@Service
public class MyService {

	private final MyProperties properties;

	public MyService(MyProperties properties) {
		this.properties = properties;
	}

	public void openConnection() {
		Server server = new Server(this.properties.getRemoteAddress());
		server.start();
	}
}
```

`@ConfigurationProperties`를 사용하면, IDE에서 자동완성을 제공할 수 있는 메타데이터 파일을 생성할 수도 있습니다. 자세한 내용은
부록([Appendix](https://docs.spring.io/spring-boot/specification/configuration-metadata/index.html))을 참고하세요.

---

이어서 부록(appendix)이나 관련 추가 문서가 필요하다면 말씀 주세요.
