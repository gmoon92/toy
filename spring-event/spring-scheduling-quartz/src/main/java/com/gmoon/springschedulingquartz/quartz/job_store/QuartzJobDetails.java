package com.gmoon.springschedulingquartz.quartz.job_store;

import com.gmoon.springschedulingquartz.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springschedulingquartz.quartz.job_store.constants.QuartzColumnValue;
import com.gmoon.springschedulingquartz.quartz.job_store.id.QuartzId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;


@Entity
@Table(name = "QUARTZ_JOB_DETAILS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzJobDetails {

  @EmbeddedId
  private Id id;

  @Column(name = "DESCRIPTION", length = QuartzColumnLength.DESCRIPTION)
  private String description;

  @Column(name = "JOB_CLASS_NAME", length = QuartzColumnLength.JOB_CLASS_NAME)
  private String JobClassName;

  @ColumnDefault(QuartzColumnValue.ENABLED)
  @Column(name = "IS_DURABLE", length = QuartzColumnLength.IS_DURABLE)
  private String useDurability;

  @ColumnDefault(QuartzColumnValue.ENABLED)
  @Column(name = "IS_NONCONCURRENT", length = QuartzColumnLength.IS_NONCONCURRENT)
  private String useNonConcurrent;

  @ColumnDefault(QuartzColumnValue.ENABLED)
  @Column(name = "IS_UPDATE_DATA", length = QuartzColumnLength.IS_UPDATE_DATA)
  private String useUpdateData;

  @ColumnDefault(QuartzColumnValue.ENABLED)
  @Column(name = "REQUESTS_RECOVERY", length = QuartzColumnLength.REQUESTS_RECOVERY)
  private String useRequestsRecovery;

  @Lob
  @Column(name = "JOB_DATA", columnDefinition = "BLOB")
  private byte[] jobData;

  @Embeddable
  @ToString(callSuper = true)
  @EqualsAndHashCode(callSuper = true)
  private static class Id extends QuartzId {

    @Column(name = "JOB_NAME", length = QuartzColumnLength.JOB_NAME)
    private String jobName;

    @Column(name = "JOB_GROUP", length = QuartzColumnLength.JOB_GROUP_NAME)
    private String jobGroup;
  }
}
