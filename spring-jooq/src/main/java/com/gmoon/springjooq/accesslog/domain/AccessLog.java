package com.gmoon.springjooq.accesslog.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.gmoon.springjooq.accesslog.domain.vo.OperatingSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "tb_access_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString
public class AccessLog implements Serializable {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(name = "id", length = 50)
	private String id;

	@Column(name = "username", length = 50)
	private String username;

	@Column(name = "ip", length = 100)
	private String ip;

	@Enumerated(EnumType.STRING)
	@Column(name = "os", length = 30)
	private OperatingSystem os;

	@CreatedDate
	@Column(name = "attempt_dt", updatable = false)
	private LocalDateTime attemptDt;

	protected AccessLog(String username, String ip, OperatingSystem os) {
		this.username = username;
		this.ip = ip;
		this.os = os;
	}
}
