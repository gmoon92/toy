package com.gmoon.springjooq.accesslog.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.gmoon.springjooq.accesslog.domain.vo.OperatingSystem;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EntityListeners(AuditingEntityListener.class)
@Table(name = "tb_access_log")
@Entity
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
