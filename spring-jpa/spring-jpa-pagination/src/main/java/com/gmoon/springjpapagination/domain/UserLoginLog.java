package com.gmoon.springjpapagination.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "lt_user_login")
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginLog implements Serializable {
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
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
