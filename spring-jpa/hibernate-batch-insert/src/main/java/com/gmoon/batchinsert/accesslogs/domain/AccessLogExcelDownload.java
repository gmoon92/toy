package com.gmoon.batchinsert.accesslogs.domain;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.gmoon.javacore.util.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "ex_access_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString
public class AccessLogExcelDownload implements Serializable {

	@Id
	@Column(length = 50)
	private String id;

	@Column(length = 50)
	private String username;

	@Column(length = 100)
	private String ip;

	@Column(length = 30)
	private String os;

	@Column(length = 50)
	private String attemptDt;

	@Builder
	private AccessLogExcelDownload(String username, String ip, String os, String attemptDt) {
		this.id = UUID.randomUUID().toString();
		this.username = StringUtils.defaultString(username);
		this.ip = StringUtils.defaultString(ip);
		this.os = StringUtils.defaultString(os);
		this.attemptDt = StringUtils.defaultString(attemptDt);
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
