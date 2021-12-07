package com.gmoon.springschedulingquartz.quartz.job_store.id;

import com.gmoon.springschedulingquartz.quartz.job_store.constants.QuartzColumnLength;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@Embeddable
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class QuartzTriggerId extends QuartzId {

  @Column(name = "TRIGGER_NAME", length = QuartzColumnLength.TRIGGER_NAME)
  private String triggerName;

  @Column(name = "TRIGGER_GROUP", length = QuartzColumnLength.TRIGGER_GROUP)
  private String triggerGroup;

  public static QuartzTriggerId create(String schedulerName, String triggerName, String triggerGroup) {
    QuartzTriggerId id = new QuartzTriggerId();
    id.schedulerName = schedulerName;
    id.triggerName = triggerName;
    id.triggerGroup = triggerGroup;
    return id;
  }
}
