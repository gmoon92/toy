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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "QUARTZ_SIMPLE_TRIGGERS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzSimpleTriggers {
  @EmbeddedId
  private QuartzTriggerId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
          @JoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME", insertable = false, updatable = false),
          @JoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME", insertable = false, updatable = false),
          @JoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP", insertable = false, updatable = false)
  })
  private QuartzTriggers triggers;

  @Column(name = "REPEAT_COUNT", length = QuartzColumnLength.REPEAT_COUNT)
  private Long repeatCount;

  @Column(name = "REPEAT_INTERVAL", length = QuartzColumnLength.REPEAT_INTERVAL)
  private Long repeatInterval;

  @Column(name = "TIMES_TRIGGERED", length = QuartzColumnLength.TIMES_TRIGGERED)
  private Long timesTriggered;
}
