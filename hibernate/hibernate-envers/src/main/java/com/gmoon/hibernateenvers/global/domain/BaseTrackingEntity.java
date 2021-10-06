package com.gmoon.hibernateenvers.global.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

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

