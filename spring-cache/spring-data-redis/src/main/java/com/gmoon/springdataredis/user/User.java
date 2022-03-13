package com.gmoon.springdataredis.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tb_user")
@EqualsAndHashCode(of = "username")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements Serializable {
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "username", nullable = false, length = 50)
	private String username;

	@Column(name = "email", length = 500)
	private String email;

	@ColumnDefault("0")
	@Column(name = "enabled", nullable = false)
	private boolean enabled;
}
