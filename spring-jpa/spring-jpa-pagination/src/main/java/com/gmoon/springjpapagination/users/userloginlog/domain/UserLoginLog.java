package com.gmoon.springjpapagination.users.userloginlog.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lt_user_login", indexes = {
	 @Index(name = "idx_username", columnList = "username"),
	 @Index(name = "idx_attempt_dt", columnList = "attempt_dt")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserLoginLog implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String id;

	@Column(length = 50)
	private String username;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	@ColumnDefault("'WEB'")
	private AccessDevice accessDevice;

	@Column(length = 50)
	private String attemptIp;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime attemptDt;

	@ColumnDefault("0")
	private Boolean succeed;

	@Builder
	private UserLoginLog(String username, AccessDevice accessDevice, String attemptIp,
		 LocalDateTime attemptDt, Boolean succeed) {
		this.id = id;
		this.username = username;
		this.accessDevice = accessDevice;
		this.attemptIp = attemptIp;
		this.attemptDt = attemptDt;
		this.succeed = succeed;
	}
}
