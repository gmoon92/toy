package com.gmoon.springschedulingquartz.job_store;

import com.gmoon.springschedulingquartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springschedulingquartz.job_store.id.QuartzId;
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

@Entity
@Table(name = "QUARTZ_PAUSED_TRIGGER_GRPS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzPausedTriggerGrps {

  @EmbeddedId
  private Id id;

  @Embeddable
  @ToString(callSuper = true)
  @EqualsAndHashCode(callSuper = true)
  private static class Id extends QuartzId {

    @Column(name = "TRIGGER_GROUP", length = QuartzColumnLength.TRIGGER_GROUP)
    private String triggerGroup;
  }
}
