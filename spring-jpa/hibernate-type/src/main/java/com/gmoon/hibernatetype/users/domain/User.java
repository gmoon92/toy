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

import com.gmoon.hibernatetype.global.type.ColumnEncryptionConstants;
import com.gmoon.hibernatetype.global.type.EncryptStringType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@TypeDef(name = "encStr", typeClass = EncryptStringType.class)
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

	@ColumnTransformer(
		read = ColumnEncryptionConstants.DEC_EMAIL,
		write = ColumnEncryptionConstants.ENC_COLUMN
	)
	@Column(length = 200, name = "email")
	private String email;


	// @Type(type = "com.gmoon.hibernatetype.global.type.EncryptStringType")
	@Type(type = "encStr")
	@Column(length = 200, name = "enc_email")
	private String encEmail;

	@Builder
	private User(String email, String encEmail) {
		this.email = email;
		this.encEmail = encEmail;
	}
}
