package com.gmoon.springschedulingquartz.job_store;

import com.gmoon.springschedulingquartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springschedulingquartz.job_store.constants.QuartzColumnValue;
import com.gmoon.springschedulingquartz.job_store.id.QuartzId;
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
import javax.persistence.Table;

@Entity
@Table(name = "QUARTZ_FIRED_TRIGGERS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzFiredTriggers {

  @EmbeddedId
  private Id id;

  @Column(name = "TRIGGER_NAME", length = QuartzColumnLength.TRIGGER_NAME, nullable = false)
  private String triggerName;

  @Column(name = "TRIGGER_GROUP", length = QuartzColumnLength.TRIGGER_GROUP, nullable = false)
  private String triggerGroup;

  @Column(name = "INSTANCE_NAME", length = QuartzColumnLength.INSTANCE_NAME, nullable = false)
  private String instanceName;

  @Column(name = "FIRED_TIME", length = QuartzColumnLength.FIRED_TIME, nullable = false)
  private long firedTime;

  @Column(name = "SCHED_TIME", length = QuartzColumnLength.SCHED_TIME, nullable = false)
  private long schedulerTime;

  @Column(name = "PRIORITY", nullable = false)
  private int priority;

  @Column(name = "STATE", length = QuartzColumnLength.SATE ,nullable = false)
  private String state;

  @Column(name = "JOB_NAME", length = QuartzColumnLength.JOB_NAME, nullable = false)
  private String jobName;

  @Column(name = "JOB_GROUP", length = QuartzColumnLength.JOB_GROUP, nullable = false)
  private String jobGroup;

  @ColumnDefault(QuartzColumnValue.ENABLED)
  @Column(name = "IS_NONCONCURRENT", length = QuartzColumnLength.IS_NONCONCURRENT, nullable = false)
  private String useNonConcurrent;

  @ColumnDefault(QuartzColumnValue.ENABLED)
  @Column(name = "REQUESTS_RECOVERY", length = QuartzColumnLength.REQUESTS_RECOVERY, nullable = false)
  private String useRequestsRecovery;

  @Embeddable
  @ToString(callSuper = true)
  @EqualsAndHashCode(callSuper = true)
  private static class Id extends QuartzId {

    @Column(name = "ENTRY_ID", length = QuartzColumnLength.ENTRY_ID)
    private String entryId;
  }
}
