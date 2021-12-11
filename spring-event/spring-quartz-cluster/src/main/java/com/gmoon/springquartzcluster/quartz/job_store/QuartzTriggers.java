package com.gmoon.springquartzcluster.quartz.job_store;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springquartzcluster.quartz.job_store.id.QuartzTriggerId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "QUARTZ_TRIGGERS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzTriggers {
  @EmbeddedId
  private QuartzTriggerId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
          @JoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME", insertable = false, updatable = false),
          @JoinColumn(name = "JOB_NAME", referencedColumnName = "JOB_NAME", insertable = false, updatable = false),
          @JoinColumn(name = "JOB_GROUP", referencedColumnName = "JOB_GROUP", insertable = false, updatable = false)
  })
  private QuartzJobDetails jobDetails;

  @Column(name = "JOB_NAME", length = QuartzColumnLength.JOB_NAME)
  private String jobName;

  @Column(name = "JOB_GROUP", length = QuartzColumnLength.JOB_GROUP)
  private String jobGroup;

  @Column(name = "DESCRIPTION", length = QuartzColumnLength.DESCRIPTION)
  private String description;

  @Column(name = "NEXT_FIRE_TIME", length = QuartzColumnLength.NEXT_FIRE_TIME)
  private Long nextFireTime;

  @Column(name = "PREV_FIRE_TIME", length = QuartzColumnLength.PREV_FIRE_TIME)
  private Long prevFireTime;

  @Column(name = "PRIORITY")
  private Integer priority;

  @Column(name = "TRIGGER_STATE", length = QuartzColumnLength.TRIGGER_STATE)
  private String state;

  @Column(name = "TRIGGER_TYPE", length = QuartzColumnLength.TRIGGER_TYPE)
  private String type;

  @Column(name = "START_TIME", length = QuartzColumnLength.START_TIME)
  private Long startTime;

  @Column(name = "END_TIME", length = QuartzColumnLength.END_TIME)
  private Long endTime;

  @Column(name = "CALENDAR_NAME", length = QuartzColumnLength.CALENDAR_NAME)
  private String calendarName;

  @Column(name = "MISFIRE_INSTR", length = QuartzColumnLength.MISFIRE_INSTR)
  private Short misfireInstr;

  @Lob
  @Column(name = "JOB_DATA", columnDefinition = "BLOB")
  private byte[] jobData;
}
