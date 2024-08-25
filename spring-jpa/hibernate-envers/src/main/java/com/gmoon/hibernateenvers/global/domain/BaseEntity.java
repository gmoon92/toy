package com.gmoon.hibernateenvers.global.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public abstract class BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = -5548493302979567230L;

	@CreatedBy
	@Column(updatable = false)
	private String createdBy;

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;

	@LastModifiedBy
	private String modifiedBy;

	@LastModifiedDate
	private Instant modifiedAt;
}

