package com.gmoon.springjpaspecs.global.domain;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public abstract class AuditedEntityObject<ID extends Serializable> extends EntityObject<ID> {

	@CreatedDate
	@Column
	private Instant createdAt;

	@LastModifiedDate
	@Column
	private Instant modifiedAt;
}
