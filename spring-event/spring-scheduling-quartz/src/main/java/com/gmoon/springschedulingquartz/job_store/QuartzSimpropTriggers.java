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
@Table(name = "QUARTZ_CRON_TRIGGERS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzSimpropTriggers {

  @EmbeddedId
  private Id id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
          @JoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME", insertable = false, updatable = false),
          @JoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME", insertable = false, updatable = false),
          @JoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP", insertable = false, updatable = false)
  })
  private QuartzTriggers triggers;

  @Column(name = "STR_PROP_1", length = QuartzColumnLength.STR_PROP_1)
  private String strProp1;

  @Column(name = "STR_PROP_2", length = QuartzColumnLength.STR_PROP_2)
  private String strProp2;

  @Column(name = "STR_PROP_3", length = QuartzColumnLength.STR_PROP_3)
  private String strProp3;

  @Column(name = "INT_PROP_1")
  private Integer intProp1;

  @Column(name = "INT_PROP_2")
  private Integer intProp2;

  @Column(name = "LONG_PROP_1")
  private Long longProp1;

  @Column(name = "LONG_PROP_2")
  private Long longProp2;

  @Column(name = "DEC_PROP_1")
  private Long decProp1;

  @Column(name = "DEC_PROP_2")
  private Long decProp2;

  @Column(name = "BOOL_PROP_1", length = QuartzColumnLength.BOOL_PROP_1)
  private String boolProp1;

  @Column(name = "BOOL_PROP_2", length = QuartzColumnLength.BOOL_PROP_2)
  private String boolProp2;

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
