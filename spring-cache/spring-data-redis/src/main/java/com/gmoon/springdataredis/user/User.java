package com.gmoon.springdataredis.user;

import java.io.Serializable;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "username")
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
