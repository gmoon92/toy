# Hibernate Type

## [@ColumnTransformer](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#annotations-hibernate-columntransformer)

`@ColumnTransformer`는 테이블 컬럼에 대해 read/write 를 재정의하는 데 사용한다.

예를 들어 사용자의 개인정보 관련 컬럼의 암호화 목적으로 사용할 수 있다.

```java
@Entity
public class User {

	@Id
	@Column(length = 50)
	private String id;

	@ColumnTransformer(
	  read = "CAST(AES_DECRYPT(UNHEX(email), 'SECRETKEY') AS CHAR)",
	  write = "HEX(AES_ENCRYPT(?, 'SECRETKEY'))"
	)
	@Column(length = 200, name = "email")
	private String email;
}
```
```sql
-- SELECT
SELECT id,
       CAST(AES_DECRYPT(UNHEX(email), 'SECRETKEY') AS CHAR) AS col_0_0_ 
FROM tb_user

-- INSERT
INSERT INTO tb_user (id, email)
VALUES (uuid(), HEX(AES_ENCRYPT('gmoon92@gmail.com', 'SECRETKEY')))
```

### 암호화 데이터의 조회 성능

하이버네이트에서 다음과 같은 쿼리를 생성하게 된다. 암복호화에 대한 고민을 덜 수 있다.

다만 암호화된 컬럼을 검색한다면, 조회 성능에 대한 고민이 필요하다.

```sql
select CAST(AES_DECRYPT(UNHEX(email), 'SECRETKEY') AS CHAR) 
from tb_user
where CAST(AES_DECRYPT(UNHEX(email), 'SECRETKEY') AS CHAR) = 'gmoon92@gmail.com';
```

데이터베이스 벤더마다 정의된 단일 컬럼 인덱스의 인덱스 최대 키 길이는 다르므로, 암호화된 데이터 크기에 따라서 인덱스 관리에 제약이 있다.

> 암호화된 컬럼에 고정된 길이에 대한 고민이 있다면, 해쉬 알고리즘(MD5, SHA, HMAC...)을 활용하면 데이터 길이를 제약할 수 있다. 하지만 단방향 암호화로 복호화할 수 없음으로 요구 사항에 따라 선택해야한다.

암호화 컬럼을 인덱스로 활용할 수 있더라도, 하이버네이트가 생성한 조회 쿼리는 컬럼을 복호한 후 동등 비교를 하기 때문에 인덱스를 탈 수 없는 구조다.

`where CAST(AES_DECRYPT(UNHEX(email), 'SECRETKEY') AS CHAR) = '?';`

따라서 아래 조회 쿼리처럼 암호화 데이터 자체를 동등 비교해서 인덱스를 탈 수 있도록 수정해야 한다.

```sql
select * 
from tb_user
where email = HEX(AES_ENCRYPT('gmoon92@gmail.com', 'SECRETKEY'))
```

## [BasicTypeRegistry](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#basic-legacy-registry)

하이버네이트에서 SQL 타입과의 Java 타입간의 마샬링을 하는 부분을 [BasicTypeRegistry](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#basic-legacy-registry)에서 관리한다.

암호화된 데이터는 조회 쿼리 필드는 복호화하고, 쿼리 파라미터는 암호화되어 인식되도록 커스텀한 타입을 `BasicTypeRegistry`에 등록해야한다.

하이버네이트는 커스텀 BasicType을 등록할 수 있도록 [@org.hibernate.annotations.Type](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#annotations-hibernate-type) 를 지원하고 있다.

### [Custom BasicTypes](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#basic-custom-type)

`org.hibernate.type.BasicType`를 구현하기엔 많은 메서드를 구현해야되기 때문에, 하이버네이트에서 지원하는 추상화 팩토리 패턴으로 구현된 클래스를 사용하면 편리하다.

- `AbstractStandardBasicType`
- `AbstractSingleColumnStandardBasicType` 

```java
public class EncryptedStringType
	extends AbstractSingleColumnStandardBasicType<String>
	implements DiscriminatorType<String> {

	public EncryptedStringType() {
		super(VarcharTypeDescriptor.INSTANCE, EncryptedStringTypeDescriptor.INSTANCE);
	}

	@Override
	public String stringToObject(String xml) throws Exception {
		return fromString(xml);
	}

	@Override
	public String objectToSQLString(String value, Dialect dialect) throws Exception {
		return toString(value);
	}

	@Override
	public String getName() {
		return "encryptString";
	}
}
```

다음으로 하이버네이트가 Java 타입을 관리할 수 있도록 `org.hibernate.type.descriptor.java.JavaDescriptor` 를 구현해야된다.

`JavaDescriptor`는 `AbstractTypeDescriptor`를 사용하여 구현한다.

- org.hibernate.type.descriptor.java.AbstractTypeDescriptor

```java
public class EncryptedStringTypeDescriptor extends AbstractTypeDescriptor<String> {

	public static final EncryptedStringTypeDescriptor INSTANCE = new EncryptedStringTypeDescriptor();

	@SuppressWarnings({"unchecked"})
	private EncryptedStringTypeDescriptor() {
		super(String.class, ImmutableMutabilityPlan.INSTANCE);
	}

	@Override
	public String fromString(String string) {
		return string;
	}

	// java -> sql
	@SuppressWarnings({"unchecked"})
	@Override
	public <X> X unwrap(String value, Class<X> type, WrapperOptions options) {
		String encrypted = encrypt(value);
		return StringTypeDescriptor.INSTANCE.unwrap(encrypted, type, options);
	}

	private String encrypt(String value) {
		try {
			return CryptoUtils.encrypt(value, ColumnEncryptionConstants.KEY_SPEC);
		} catch (Exception e) {
			return value;
		}
	}

	// sql -> java
	@Override
	public <X> String wrap(X value, WrapperOptions options) {
		X decrypt = decrypt(value);
		return StringTypeDescriptor.INSTANCE.wrap(decrypt, options);
	}

	private <X> X decrypt(X value) {
		try {
			return (X)CryptoUtils.decrypt((String)value, ColumnEncryptionConstants.KEY_SPEC);
		} catch (Exception e) {
			return value;
		}
	}
}

```

Java 타입을 SQL로 변환하기 위해 `wrap`, `unwrap` 메서드를 재정의해야 한다.

- wrap: SQL -> Java
  - 쿼리 필드 복호화
- unwrap: Java -> SQL
  - 쿼리 파라미터 암호화

> org.hibernate.usertype.UserType 을 구현하여 변환 과정을 제어할 수도 있다. 

### @Type

구현한 커스텀 타입을 엔티티와 매핑 시켜주자.

- TypeDef
- Type

> Hibernate 6 버전부터 @Type 어노테이션은 `deprecated`되어 @Convert 어노테이션을 고려하자. [Basic Type Contributor](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#basic-type-contributor) 섹션을 참고하자.

```java
import org.hibernate.annotations.TypeDef;

@TypeDef(
  name = "secure", 
  typeClass = EncryptedStringType.class
)
@Entity
public class User {

	@Id
	@Column(length = 50)
	private String id;

	// @ColumnTransformer(
	//   read = "CAST(AES_DECRYPT(UNHEX(email), 'SECRETKEY') AS CHAR)",
	//   write = "HEX(AES_ENCRYPT(?, 'SECRETKEY'))"
	// )
	@Type(type = "secure")
	@Column(length = 200, name = "email")
	private String email;
}
```

## 검증

이제 다음 쿼리가 어떻게 생성되는지 확인해보자.

```java
@Repository
@RequiredArgsConstructor
public class UserRepositoryQuery {

	private final JPAQueryFactory factory;
	
	public List<User> findAllByEncEmail(String email) {
		return factory.select(
			Projections.constructor(
			  User.class,
			  user.email
			)
		  )
		  .from(user)
		  .where(user.email.eq(email))
		  .fetch();
	}
}
```
```text
## Hibernate log
Hibernate: select user0_.email as col_0_0_ from tb_user user0_ where user0_.email=?
2024-02-02 14:08:46.002 TRACE 69309 --- [main] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [VARCHAR] - [gmoon92@gmail.com]

## MySQL log
2024-02-02 14:08:46.018  INFO 69309 --- [           main] MySQL                                    : [QUERY] select user0_.email as col_0_0_ from tb_user user0_ where user0_.email='0F087778E671EB2C66B5509835FCF5DB' [Created on: Fri Feb 02 14:08:46 KST 2024, duration: 14, connection-id: 978, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeQuery(ProxyPreparedStatement.java:52)]
```

하이버네이트 로그상으론 평문으로 파라미터가 찍히지만, 실제 쿼리는 정상적으로 암호화하여 수행된다는걸 확인해볼 수 있다.

우린 여기서 커스텀 타입으로 구현하게 되면 `ColumnTransformer`와 달리 로그상 암호화 키를 노출하고 있지 않는다는 이점을 누릴 수 있다.

- `ColumnTransformer` 는 암호화 키가 로그상 노출된다.
  - CAST(AES_DECRYPT(UNHEX(email), `'SECRETKEY'`) AS CHAR)
  - HEX(AES_ENCRYPT('gmoon92@gmail.com', `'SECRETKEY'`))

단, 애플리케이션단에서 암복호화하기 때문에 `like` 키워드를 활용한 문자열 검색은 지원할 수 없다.

## 마무리

암호화 데이터에 대해선 인덱스 관리 방식을 검토해야 한다.

또한, 구현 방식은 따라 장/단점은 명확하니 요구사항에 맞게 구현해야 한다.

|                    | eq | like | description                                                  |
|--------------------|----|------|--------------------------------------------------------------|
| @Type<br/>@Convert  | O  | X    | 인덱스 관리가 됨으로 조회 성능만 고려한다면 구현<br/>단 like 키워드를 활용한 문자열 검색 지원 불가 |
| @ColumnTransformer | O  | O    | 인덱스를 타지 않기 때문에, slow query 발생. 로그상 암호화 키 노출                  |

여담으로 암호화 데이터 검색과 관련되서 [Searchable symmetric encryption, SSE](https://en.wikipedia.org/wiki/Searchable_symmetric_encryption) 라는 기술이 존재한다.

## Reference

- [Hibernate User guid - Custom BasicTypes](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#basic-custom-type)
- baeldung
  - [Custom Types in Hibernate and the @Type Annotation](https://www.baeldung.com/hibernate-custom-types)
  - [A Guide to the Hibernate Types Library](https://www.baeldung.com/hibernate-types-library)

