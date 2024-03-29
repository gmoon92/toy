package com.gmoon.hibernateenvers.global.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTrackingEntity extends BaseEntity {

	@CreatedBy
	@Column(updatable = false)
	private String createdBy;

	@CreatedDate
	private LocalDateTime createdDt;

	@LastModifiedBy
	private String modifiedBy;

	@LastModifiedDate
	private LocalDateTime modifiedDt;
}

