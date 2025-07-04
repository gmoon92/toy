# Spring Boot 프로퍼티 설정

---

## [릴랙스 바인딩 (Relaxed Binding)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding)

Spring Boot는 `Environment` 프로퍼티를 `@ConfigurationProperties` 빈에 바인딩할 때, 엄격한 일치 대신 느슨한 규칙(relaxed rules)이 적용한다.

- `Environment` 변수 이름과 빈 프로퍼티 이름이 정확히 일치하지 않아도
된다.
- 예를 들어 하이픈(-)으로 구분된 환경 변수(`context-path`)가 카멜케이스 프로퍼티(`contextPath`)로 바인딩된다.
- 대문자 환경 변수(`PORT`)도 소문자 프로퍼티(`port`)로 바인딩된다.

```java
@ConfigurationProperties("my.main-project.person")
public class MyPersonProperties {

	private String firstName;
	
	// getter/setter
}
```

| 프로퍼티 이름                             | 설명                                        |
|-------------------------------------|-------------------------------------------|
| `my.main-project.person.first-name` | 케밥 케이스 <br/>`.properties` 파일이나 YAML 에서 권장 |
| `my.main-project.person.firstName`  | 카멜 케이스 (Java 표준)                          |
| `my.main-project.person.first_name` | 언더스코어 표기법 (대안)                            |
| `MY_MAINPROJECT_PERSON_FIRSTNAME`   | 대문자 환경 변수 표기법 (시스템 환경 변수용)                |

`@ConfigrationProperties` 애노테이션에 지정하는 prefix 값은 케밥 케이스여야 한다.(모두 소문자, 단어는 하이픈으로 구분, 예: `my.main-project.person`)

#### 각 프로퍼티 소스별 느슨한 바인딩 규칙

| 프로퍼티 소스               | 단일 값                                                       | 리스트 값                                     |
|-----------------------|------------------------------------------------------------|-------------------------------------------|
| 프로퍼티 파일 (.properties) | 카멜 케이스, 케밥 케이스, 언더스코어 표기법 모두 지원                            | [ ] 또는 콤마(,)로 구분하는 표준 리스트 구문 지원           |
| YAML 파일               | 카멜 케이스, 케밥 케이스, 언더스코어 표기법 모두 지원                            | YAML 리스트 구문 또는 콤마로 구분                     |
| 환경 변수                 | 대문자+언더스코어(예: MY_PERSON_FIRST_NAME), 자세한 규칙은 "환경 변수 바인딩" 참조 | 숫자는 언더스코어로 감싼다(예: MY_PERSONS_0_FIRSTNAME) |
| 시스템 프로퍼티              | 카멜 케이스, 케밥 케이스, 언더스코어 표기법 모두 지원                            | [ ] 또는 콤마(,)로 구분하는 표준 리스트 구문 지원           |

가능하면 프로퍼티 이름은 소문자 케밥 케이스(`my.person.first-name`)로 저장하는 것을 권장한다.

---

## [Map 바인딩](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding.maps)

`Map` 프로퍼티에 바인딩할 때는, 원래 키 값을 보존하기 위해 특별한 대괄호 표기법이 필요할 수 있다. 대괄호(`[]`)로 감싸지 않으면 알파벳, 숫자, 하이픈, 점(.) 이외 문자는 제거된다.

- 알파벳
- 숫자
- 하이픈
- 점(.)

### properties 파일에서 Map 바인딩

다음 프로퍼티를 `Map<String, String>`에 바인딩할 경우.

```properties
my.map[/key1]=value1
my.map[/key2]=value2
my.map./key3=value3
```

- `/key1`, `/key2`, `key3`이 키가 된다. 
- `key3`의 경우 슬래시가 대괄호 없이 쓰여서 제거됐다.

### YAML 파일에서 Map 바인딩

`YAML`에서는 대괄호를 쓸 경우 키를 따옴표로 감싸야 정상적으로 인식된다.

```yaml
my:
 map"[/key1]": value1
 map"[/key2]": value2
 map"[/key3]": value3
```

### 스칼라 값에 바인딩할 때(.이 포함된 키 사용)

스칼라 값(예: enum, java.lang 패키지 내 타입, Object는 예외)과 바인딩할 경우, 
키가 .(점)을 포함하더라도 대괄호로 감싸지 않아도 된다.

예를 들어, `a.b=c` 라는 프로퍼티를 Map<String, String>에 바인딩하면, `.`이 보존되어 `{"a.b"="c"}`인 Map이 생성된다.

반면, Map<String, Object>와 같이 **다른 타입에 바인딩할 때 키에 점이 포함되어 있다면 대괄호 표기를 써야 점이 보존된다.**

- `a.b=c` → Map<String, Object>에서 `{ "a"={ "b"="c" } }`로 바인딩된다.
- `[a.b]=c` → `{ "a.b"="c" }`로 바인딩된다.

### [환경 변수로부터 바인딩하기](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables)

대부분의 운영체제는 환경 변수의 이름에 제한이 많다. 

- 리눅스 쉘 환경 변수: 영문 대소문자, 숫자, 언더스코어(_)만 허용 (관례상 대문자 이름 권장)

Spring Boot의 느슨한 바인딩 규칙은 이러한 운영체제의 제약과 최대한 호환되도록 설계되어 있다. 프로퍼티 이름을 환경 변수 이름으로 변환할 때는 다음 규칙을 따른다.

- `spring.main.log-startup-info` -> `SPRING_MAIN_LOGSTARTUPINFO`
  1. 점(.)을 언더스코어(_)로 바꾼다.
  2. 대시(-)는 삭제한다.
  3. 전부 대문자로 바꾼다.

배열 또는 객체 리스트(List)에도 환경 변수를 쓸 수 있다.

- `my.service[0].other` -> `MY_SERVICE_0_OTHER`
  - 요소 번호는 언더스코어로 감싸서 변수명을 만든다.

환경 변수로부터 바인딩하는 기능은 `systemEnvironment` 프로퍼티 소스와, 이름이 `-systemEnvironment`로 끝나는 추가 프로퍼티 소스에 적용된다.

#### 환경 변수에서 Map 바인딩

Spring Boot는 환경 변수 이름을 소문자로 변환한 뒤 바인딩한다. 이 점은 특히 `Map` 바인딩에서 중요하다.

```java
@ConfigurationProperties("my.props")
public class MyMapsProperties {

	private final Map<String, String> values = new HashMap<>();

	public Map<String, String> getValues() {
		return this.values;
	}
}
```

- 환경 변수 `MY_PROPS_VALUES_KEY=value`를 설정하면, `values` Map에 `{"key"="value"}` 항목이 들어간다.
- 키만 소문자로 변환하며 값은 변환하지 않는다.
  - 예를 들어 `MY_PROPS_VALUES_KEY=VALUE`는 `{"key"="VALUE"}`가 된다.

---

### [캐싱 (Caching)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding.caching)

Spring Boot의 relaxed binding(유연한 바인딩)은 성능 향상을 위해 캐시를 사용한다. 

**기본적으로 이 캐시는 변경되지 않는(immutable) 프로퍼티 소스에만 적용된다.** 만약 변경 가능한(mutable) 프로퍼티 소스에도 캐싱을 적용하고 싶다면, `ConfigurationPropertyCaching`을 사용해서 동작을 커스터마이징할 수 있다.

---


## [복합 타입 병합 (Merging Complex Types)](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.merging-complex-types)

리스트 값이 여러 곳에서 설정되어 있다면, **가장 우선순위가 높은 리스트 값이 전체를 통째로 덮어쓴다.**

예를 들어, 기본값이 `null`인 `name`과 `description` 속성을 가진 `MyPojo` 객체 리스트를 바인딩하는 경우:

```java
@ConfigurationProperties("my")
public class MyProperties {

	private final List<MyPojo> list = new ArrayList<>();

	// getter
}
```
```properties
my.list[0].name=my name
my.list[0].description=my description
#---
spring.config.activate.on-profile=dev
my.list[0].name=my another name
```

- 기본 프로파일
    - list=[{name: "my name", description: "my description"}]
- dev 프로파일 활성화
    - list=[{name: "my another name", description: null}]

두 번째 항목이 추가되거나 기존 항목이 병합되지는 않는다.

여러 프로필에 리스트가 있을 때 **우선순위가 가장 높은 프로필의 리스트만 사용된다.**

마찬가지로 `YAML`에서도 쉼표로 구분된 리스트 또는 표준 YAML 리스트 문법 방식 모두, **리스트 전체를 덮어쓰는 방식으로 동작한다.**

---

#### [Map 병합](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.merging-complex-types)

맵(Map) 프로퍼티는 여러 소스에서 값을 가져와 바인딩할 수 있다.

그러나 동일한 키가 여러 곳에 있으면 우선순위가 높은 설정이 사용된다.

```java
@ConfigurationProperties("my")
public class MyProperties {

	private final Map<String, MyPojo> map = new LinkedHashMap<>();

	public Map<String, MyPojo> getMap() {
		return this.map;
	}
}
```
```properties
my.map.key1.name=my name 1
my.map.key1.description=my description 1
#---
spring.config.activate.on-profile=dev
my.map.key1.name=dev name 1
my.map.key2.name=dev name 2
my.map.key2.description=dev description 2
```

- 기본 프로파일
    - map={"key1":{name:"my name 1", description:"my description 1"}}
- dev 프로파일
  - map={"key1":{name:"dev name 1", description:null}, "key2":{name:"dev name 2", description:dev description 2}}

---

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
