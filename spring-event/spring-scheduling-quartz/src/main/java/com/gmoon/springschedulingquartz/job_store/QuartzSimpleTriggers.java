package com.gmoon.springschedulingquartz.job_store;

import com.gmoon.springschedulingquartz.job_store.constants.QuartzColumnLength;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "QUARTZ_SIMPLE_TRIGGERS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzSimpleTriggers {

  @EmbeddedId
  private Id id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
          @JoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME", insertable = false, updatable = false),
          @JoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME", insertable = false, updatable = false),
          @JoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP", insertable = false, updatable = false)
  })
  private QuartzTriggers triggers;

  @Column(name = "REPEAT_COUNT", length = QuartzColumnLength.REPEAT_COUNT, nullable = false)
  private Long repeatCount;

  @Column(name = "REPEAT_INTERVAL", length = QuartzColumnLength.REPEAT_INTERVAL, nullable = false)
  private Long repeatInterval;

  @Column(name = "TIMES_TRIGGERED", length = QuartzColumnLength.TIMES_TRIGGERED, nullable = false)
  private Long timesTriggered;

  @Embeddable
  @ToString
  @EqualsAndHashCode
  private static class Id implements Serializable {
    @Column(name = "SCHED_NAME", length = QuartzColumnLength.SCHEDULER_NAME)
    private String schedulerName;

    @Column(name = "TRIGGER_NAME", length = QuartzColumnLength.TRIGGER_NAME)
    private String triggerName;

    @Column(name = "TRIGGER_GROUP", length = QuartzColumnLength.TRIGGER_GROUP)
    private String triggerGroup;
  }
}
