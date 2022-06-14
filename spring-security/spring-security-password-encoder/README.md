## PasswordEncoder

Spring Security 5.0 이전에 기본 `PasswordEncoder`는 `NoOpPasswordEncoder` 였다.

다음과 같이 패스워드 암호화 방식에 따라 빈을 등록해줘야만 했다.

```java
@Bean
public PasswordEncoder passwordEncoder(){
  return new BCryptPasswordEncoder();
  }
```

하지만 암호화 방식은 언제나 보안 취약점이 발견될 수 있다. 예를 들어 `SHA-1` 같이 보안상 문제가 되어 암호화 방식을 변경해야만 한다면 어떻게 해야될까?

일반적으로 패스워드는 단방향 알고리즘을 사용하여 암호화하기 때문에, 추후 변경된 암호화 방식으로 마이그레이션하기에 매우 까다롭다.

- 변경된 암호화 방식으로 쉽게 마이그레이션할 수 없다.
- 현재 사용하고 있는 암호화 방식의 취약점은 언제든 발견될 수 있다.

앞서 본 문제를 해결하기 위해 Spring Security 5.0 이후 버전 부터 기본적으로 DelegatingPasswordEncoder 를 사용한다.

## [DelegatingPasswordEncoder](https://docs.spring.io/spring-security/reference/5.7.2/features/authentication/password-storage.html#authentication-password-storage-dpe)

Spring Security 에서 지원하는 `DelegatingPasswordEncoder` 는 동시에 여러 암호화 알고리즘을 사용할 수 있도록 지원한다.

- 현재 패스워드가 애플리케이션에서 권장하고 있는 암호화 방식으로 인코딩되었는지 확인
- 패스워드 검증시 예전 암호화 방식과 마이그레이션할 암호화 방식 유효성 검사 가능
- 변경된 암호화 방식으로 마이그레이션 지원

### DelegatingPasswordEncoder 생성

`DelegatingPasswordEncoder`는 `PasswordEncoderFactories` 를 사용하여 쉽게 인스턴스를 생성할 수 있도록 지원하고 있다.

```java
public class Application {
  public static void main(String[] args) {
    // [1] PasswordEncoderFactories 으로 생성
    PasswordEncoder passwordEncoder1 = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    // [2] Custom, 인코딩 id 및 패스워드 인코더 지정 생성
    String idForEncode = "bcrypt";
    Map encoders = new HashMap<>();
    encoders.put(idForEncode, new BCryptPasswordEncoder());
    encoders.put("noop", NoOpPasswordEncoder.getInstance());
    encoders.put("sha256", new StandardPasswordEncoder());

    PasswordEncoder passwordEncoder2 = new DelegatingPasswordEncoder(idForEncode, encoders);
  }\
}
```

### 패스워드 형식

```java
public class DelegatingPasswordEncoder {
  @Override
  public String encode(CharSequence rawPassword) {
    return PREFIX + this.idForEncode + SUFFIX + this.passwordEncoderForEncode.encode(rawPassword);
  }
}
```

```text
{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
```

- 패스워드 문자열 생성시 사용된 prefix(암호화 id) 포함하여 생성
    - `{id}encodedPassword`
    - id 는 `PasswordEncoder`를 찾기 위한 식별자 역할
    - 인코딩시 prefix 가 포함되어 있지 않다면 IllegalArgumentException 예외 반환

### setDefaultPasswordEncoderForMatches

```java
public class Application {
	public static void main(String[] args) {
		DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(idForEncode, encoders);

		// default 패스워드 인코더 지정
		//     패스워드 문자열에 prefix(암호화 id)가 없을 경우 지정된 BCryptPasswordEncoder 로 암호화.
		//     패스워드 encode prefix 를 포함하여 생성
		passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
	}
}
```

## Reference

- [Spring Security Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.security)
- [Spring Security Reference - Password Storage](https://docs.spring.io/spring-security/reference/5.7.2/features/authentication/password-storage.html#authentication-password-storage-dpe)
