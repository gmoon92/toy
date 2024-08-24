package com.gmoon.batchinsert.accesslogs.domain;

import java.io.Serializable;
import java.time.Instant;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.gmoon.batchinsert.accesslogs.domain.vo.OperatingSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "tb_access_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AccessLog implements Serializable {

	@Id
	@UuidGenerator
	@Column(name = "id", length = 50)
	private String id;

	@Column(length = 50)
	private String username;

	@Column(length = 100)
	private String ip;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private OperatingSystem os;

	@CreatedDate
	@Column(updatable = false)
	private Instant attemptAt;

	protected AccessLog(String username, String ip, OperatingSystem os) {
		this.username = username;
		this.ip = ip;
		this.os = os;
	}
}
