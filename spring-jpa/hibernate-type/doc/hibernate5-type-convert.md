# Hibernate5 type convert

- AbstractTypeDescriptor
- DiscriminatorType
- @Type
- @TypeDef

### AbstractTypeDescriptor

```java
package com.gmoon.hibernatetype.global.type;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;

import com.gmoon.hibernatetype.global.crypt.CryptoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptedStringTypeDescriptor extends AbstractTypeDescriptor<String> {
	public static final EncryptedStringTypeDescriptor INSTANCE = new EncryptedStringTypeDescriptor();

	@SuppressWarnings({"unchecked"})
	private EncryptedStringTypeDescriptor() {
		super(String.class, ImmutableMutabilityPlan.INSTANCE);
	}

	@Override
	public String fromString(String string) {
		log.info("fromString: {}", string);
		return string;
	}

	// java -> sql
	@SuppressWarnings({"unchecked"})
	@Override
	public <X> X unwrap(String value, Class<X> type, WrapperOptions options) {
		log.info("unwrap value: {}, type: {}, options: {}", value, type, options);
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
		log.info("wrap value: {}, options: {}", value, options);
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

### DiscriminatorType, AbstractSingleColumnStandardBasicType

```java
package com.gmoon.hibernatetype.global.type;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptedStringType
	 extends AbstractSingleColumnStandardBasicType<String>
	 implements DiscriminatorType<String> {

	public EncryptedStringType() {
		super(VarcharTypeDescriptor.INSTANCE, EncryptedStringTypeDescriptor.INSTANCE);
	}

	@Override
	public String stringToObject(String xml) throws Exception {
		log.info("xml: {}", xml);
		return fromString(xml);
	}

	@Override
	public String objectToSQLString(String value, Dialect dialect) throws Exception {
		log.info("toSqlString: {}", value);
		return toString(value);
	}

	@Override
	public String getName() {
		return "encryptedString";
	}
}

```

### @Type

```java
package com.gmoon.hibernatetype.users.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.querydsl.core.annotations.QueryProjection;

import com.gmoon.hibernatetype.global.type.ColumnEncryptionConstants;
import com.gmoon.hibernatetype.global.type.EncryptedStringType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@TypeDef(name = "secure", typeClass = EncryptedStringType.class)
@Entity
@Table(name = "tb_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class User implements Serializable {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(length = 50)
	private String id;

	// select user0_.id as id1_0_, CAST(AES_DECRYPT(UNHEX(user0_.email), 'SECRETKEY') AS CHAR) as email2_0_, user0_.enc_email as enc_emai3_0_
	// from tb_user user0_ where ()user0_.email=?
	@ColumnTransformer(
		 read = ColumnEncryptionConstants.DEC_EMAIL,
		 write = ColumnEncryptionConstants.ENC_COLUMN
	)
	@Column(length = 200, name = "email")
	private String email;

	// @Type(type = "com.gmoon.hibernatetype.global.type.EncryptStringType")
	@Type(type = "secure")
	@Column(length = 200, name = "enc_email")
	private String encEmail;

	@Builder
	@QueryProjection
	public User(String email, String encEmail) {
		this.email = email;
		this.encEmail = encEmail;
	}
}

```

## Reference

