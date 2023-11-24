package com.gmoon.batchinsert.accesslogs.domain;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EntityListeners(AuditingEntityListener.class)
@Table(name = "ex_access_log")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString
public class AccessLogExcelDownload implements Serializable {

	@Id
	@Column(name = "id", length = 50)
	private String id;

	@Column(name = "username", length = 50)
	private String username;

	@Column(name = "ip", length = 100)
	private String ip;

	@Column(name = "os", length = 30)
	private String os;

	@Column(name = "attempt_dt", length = 50)
	private String attemptDt;

	@Builder
	private AccessLogExcelDownload(String username, String ip, String os, String attemptDt) {
		this.id = UUID.randomUUID().toString();
		this.username = username;
		this.ip = ip;
		this.os = os;
		this.attemptDt = attemptDt;
	}

	public static AccessLogExcelDownload create(AccessLog accessLog) {
		return AccessLogExcelDownload.builder()
			.username(accessLog.getUsername())
			.ip(accessLog.getIp())
			.os(accessLog.getOs().name())
			.attemptDt(accessLog.getAttemptDt().format(DateTimeFormatter.BASIC_ISO_DATE))
			.build();
	}
}
