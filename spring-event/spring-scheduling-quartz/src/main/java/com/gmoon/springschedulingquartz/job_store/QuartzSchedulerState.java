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
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "QUARTZ_SCHEDULER_STATE")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzSchedulerState {

  @EmbeddedId
  private Id id;

  @Column(name = "LAST_CHECKIN_TIME", length = QuartzColumnLength.LAST_CHECKIN_TIME)
  private Long lastCheckinTime;

  @Column(name = "CHECKIN_INTERVAL", length = QuartzColumnLength.CHECKIN_INTERVAL)
  private Long checkinInterval;

  @Embeddable
  @ToString
  @EqualsAndHashCode
  private static class Id implements Serializable {
    @Column(name = "SCHED_NAME", length = QuartzColumnLength.SCHEDULER_NAME)
    private String schedulerName;

    @Column(name = "INSTANCE_NAME", length = QuartzColumnLength.INSTANCE_NAME)
    private String instanceName;
  }
}
