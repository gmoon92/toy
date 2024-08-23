package com.gmoon.hibernatetype.users.domain;

import java.io.Serializable;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import com.querydsl.core.annotations.QueryProjection;

import com.gmoon.hibernatetype.global.type.ColumnEncryptionConstants;
import com.gmoon.hibernatetype.global.type.EncryptedStringType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

// @Convert(attributeName = "secure", converter = EncryptedStringType.class)
@Entity
@Table(name = "tb_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class User implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String id;

	// select user0_.id as id1_0_, CAST(AES_DECRYPT(UNHEX(user0_.email), 'SECRETKEY') AS CHAR) as email2_0_, user0_.enc_email as enc_emai3_0_
	// from tb_user user0_ where ()user0_.email=?
	@ColumnTransformer(
		 read = ColumnEncryptionConstants.DEC_EMAIL,
		 write = ColumnEncryptionConstants.ENC_COLUMN
	)
	@Column(length = 200)
	private String email;

	// @Type(type = "com.gmoon.hibernatetype.global.type.EncryptStringType")
	@Type(EncryptedStringType.class)
	@Column(length = 200)
	private String encEmail;

	@Builder
	@QueryProjection
	public User(String email, String encEmail) {
		this.email = email;
		this.encEmail = encEmail;
	}
}
