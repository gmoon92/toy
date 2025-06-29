## [서드파티 설정 (Third-party Configuration)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.third-party-configuration)

클래스에 `@ConfigurationProperties`를 붙이는 것뿐만 아니라, 공개된 `@Bean` 메서드에도 붙일 수 있다. 이 방식은 특히 내 제어권 밖에 있는 서드파티 컴포넌트에 프로퍼티를 바인딩할 때
유용하다.

환경(Environment) 프로퍼티를 빈에 바인딩하려면, 해당 빈 등록 시 `@ConfigurationProperties`를 붙이면 된다. 예시는 다음과 같다:

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

위 예제에서 `another` 접두사(prefix)를 갖는 모든 JavaBean 프로퍼티가 `AnotherComponent` 빈에 매핑된다. 앞서 본 `SomeProperties` 예시와 유사하다.

---

## 릴랙스 바인딩 (Relaxed Binding)

Spring Boot는 환경 프로퍼티를 `@ConfigurationProperties` 빈에 바인딩할 때 엄격한 일치 대신 느슨한 규칙을 적용한다. 따라서 환경 변수 이름과 빈 프로퍼티 이름이 정확히 일치하지 않아도
된다.

예를 들어, 하이픈(-)으로 구분된 환경 변수(`context-path`)가 카멜케이스 프로퍼티(`contextPath`)로 바인딩되는 경우가 이에 해당한다. 대문자 환경 변수(`PORT`)도 소문자 프로퍼티(
`port`)로 바인딩된다.

다음 `@ConfigurationProperties` 클래스 예를 보자:

```java
@ConfigurationProperties("my.main-project.person")
public class MyPersonProperties {

	private String firstName;

	public String getFirstName() { return this.firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
}
```

위 클래스에 바인딩 가능한 프로퍼티 이름은 다음과 같다:

| 프로퍼티 이름                             | 설명                                 |
|-------------------------------------|------------------------------------|
| `my.main-project.person.first-name` | 케밥 케이스 (권장, `.properties` 및 YAML용) |
| `my.main-project.person.firstName`  | 카멜 케이스 (표준)                        |
| `my.main-project.person.first_name` | 언더스코어 표기법 (대안)                     |
| `MY_MAINPROJECT_PERSON_FIRSTNAME`   | 대문자 환경 변수 표기법 (시스템 환경 변수용)         |

애노테이션에 지정하는 prefix 값은 케밥 케이스여야 한다. (소문자, 하이픈으로 구분, 예: `my.main-project.person`)

---

### 프로퍼티 소스별 릴랙스 바인딩 규칙

| 프로퍼티 소스  | 단순 값 바인딩                    | 리스트 바인딩                                         |
|----------|-----------------------------|-------------------------------------------------|
| 프로퍼티 파일  | 카멜케이스, 케밥케이스, 언더스코어 표기 가능   | `[ ]` 표기법 또는 쉼표 구분 값 가능                         |
| YAML 파일  | 카멜케이스, 케밥케이스, 언더스코어 표기 가능   | 표준 YAML 리스트 또는 쉼표 구분 값 가능                       |
| 환경 변수    | 대문자+언더스코어 구분 (ex: `MY_VAR`) | 요소 번호를 언더스코어로 감싼 표기법 사용 (ex: `MY_LIST_0_VALUE`) |
| 시스템 프로퍼티 | 카멜케이스, 케밥케이스, 언더스코어 표기 가능   | `[ ]` 표기법 또는 쉼표 구분 값 가능                         |

가능하면 프로퍼티 이름은 소문자 케밥 케이스(`my.person.first-name`)로 저장하는 것을 권장한다.

---

## Map 바인딩

`Map` 프로퍼티에 바인딩할 때는, 원래 키 값을 보존하기 위해 특별한 대괄호 표기법이 필요할 수 있다. 대괄호(`[]`)로 감싸지 않으면 알파벳, 숫자, 하이픈, 점(.) 이외 문자는 제거된다.

예를 들어, 다음 프로퍼티를 `Map<String, String>`에 바인딩할 경우:

```properties
my.map[/key1]=value1
my.map[/key2]=value2
my.map./key3=value3
```

YAML에서는 대괄호를 쓸 경우 키를 따옴표로 감싸야 정상적으로 인식된다.

위 설정은 `/key1`, `/key2`, `key3`를 키로 하는 Map에 바인딩된다. `key3`의 경우 슬래시가 대괄호 없이 쓰여서 제거됐다.

스칼라 값(예: enum, java.lang 패키지 내 타입) 바인딩 시에는 키에 점(`.`)이 포함되어 있어도 대괄호 없이 쓸 수 있다.

예를 들어 `a.b=c`를 `Map<String, String>`에 바인딩하면 키가 `"a.b"`로 유지된다. 그러나 `Map<String, Object>`에 바인딩하면 점(`.`)이 계층 분리로 인식되어
`"a" : {"b":"c"}` 형태가 된다. 이때 `[a.b]=c`로 표기하면 `"a.b"`가 키로 유지된다.

---

## 환경 변수로부터 바인딩 (Binding From Environment Variables)

운영체제마다 환경 변수 이름 규칙이 엄격하다. 예를 들어 리눅스 쉘 변수는 알파벳 대소문자, 숫자, 언더스코어(`_`)만 허용하며, 관례상 대문자 이름을 쓴다.

Spring Boot의 릴랙스 바인딩 규칙은 이런 환경 변수 제한에 최대한 맞추어 설계됐다.

일반적인 변환 규칙은 다음과 같다:

* 점(`.`)은 언더스코어(`_`)로 치환
* 하이픈(`-`)은 제거
* 모두 대문자로 변환

예를 들어 `spring.main.log-startup-info` 프로퍼티는 환경 변수 `SPRING_MAIN_LOGSTARTUPINFO`가 된다.

배열 또는 리스트에 바인딩할 때는 인덱스를 언더스코어로 감싼다. 예를 들어 `my.service[0].other`는 `MY_SERVICE_0_OTHER` 환경 변수로 바인딩된다.

환경 변수로부터 바인딩하는 기능은 `systemEnvironment` 프로퍼티 소스와 `-systemEnvironment`로 끝나는 추가 소스에 적용된다.

---

## 환경 변수에서 Map 바인딩

Spring Boot는 환경 변수 이름을 소문자로 변환한 뒤 바인딩한다. 이 점은 특히 `Map` 바인딩에서 중요하다.

예를 들어:

```java
@ConfigurationProperties("my.props")
public class MyMapsProperties {

	private final Map<String, String> values = new HashMap<>();

	public Map<String, String> getValues() {
		return this.values;
	}
}
```

환경 변수 `MY_PROPS_VALUES_KEY=value`를 설정하면, `values` Map에 `{"key"="value"}` 항목이 들어간다.

키만 소문자로 변환하며 값은 변환하지 않는다. 예를 들어 `MY_PROPS_VALUES_KEY=VALUE`는 `{"key"="VALUE"}`가 된다.

---

## 캐싱 (Caching)

릴랙스 바인딩은 성능 향상을 위해 캐시를 사용한다. 기본적으로 이 캐시는 불변 프로퍼티 소스에만 적용된다. 가변 프로퍼티 소스에도 캐시를 적용하려면 `ConfigurationPropertyCaching`을 통해
조절할 수 있다.

---

## 복합 타입 병합 (Merging Complex Types)

리스트가 여러 설정 위치에 정의되면, 덮어쓰기는 전체 리스트 교체 방식으로 작동한다.

예를 들어, 기본값이 `null`인 `name`과 `description` 속성을 가진 `MyPojo` 객체 리스트를 바인딩하는 경우:

```java
@ConfigurationProperties("my")
public class MyProperties {

	private final List<MyPojo> list = new ArrayList<>();

	public List<MyPojo> getList() {
		return this.list;
	}
}
```

다음 설정이 있다고 하자:

```properties
my.list[0].name=my name
my.list[0].description=my description
#---
spring.config.activate.on-profile=dev
my.list[0].name=my another name
```

`dev` 프로필이 비활성화면 리스트에 `MyPojo` 한 개가 들어있고 이름은 `my name`이다. `dev` 프로필이 활성화되면 리스트에 여전히 한 개 항목만 존재하지만 이름은 `my another name`
으로 덮어쓴다. 두 번째 항목이 추가되거나 기존 항목이 병합되지는 않는다.

여러 프로필에 리스트가 있을 때 우선순위가 가장 높은 프로필의 리스트만 사용된다.

---

YAML에서는 쉼표로 구분된 리스트 또는 표준 YAML 리스트 문법을 모두 사용하여 리스트 내용을 완전히 덮어쓸 수 있다.

---

맵(Map) 프로퍼티는 여러 소스에서 값을 가져와 바인딩할 수 있다. 그러나 동일한 키가 여러 곳에 있으면 우선순위가 높은 설정이 사용된다.

예를 들어:

```java
@ConfigurationProperties("my")
public class MyProperties {

	private final Map<String, MyPojo> map = new LinkedHashMap<>();

	public Map<String, MyPojo> getMap() {
		return this.map;
	}
}
```

다음 설정이 있다고 하자:

```properties
my.map.key1.name=my name 1
my.map.key1.description=my description 1
#---
spring.config.activate.on-profile=dev
my.map.key1.name=dev name 1
my.map.key2.name=dev name 2
my.map.key2.description=dev description 2
```

`dev` 프로필 비활성화 시 `map`에는 키 `key1` 하나가 있으며 이름과 설명이 각각 기본값이다. `dev` 프로필 활성화 시에는 `key1`과 `key2` 두 항목이 존재하며, `key1`은 이름이
`dev name 1`으로 덮어쓰기 되고, `key2`는 새로 추가된다.

---

위 병합 규칙은 파일뿐 아니라 모든 프로퍼티 소스에 적용된다.
