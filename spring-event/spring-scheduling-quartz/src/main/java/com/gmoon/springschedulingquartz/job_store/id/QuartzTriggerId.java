package com.gmoon.springschedulingquartz.job_store.id;

import com.gmoon.springschedulingquartz.job_store.constants.QuartzColumnLength;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class QuartzTriggerId extends QuartzId {

  @Column(name = "TRIGGER_NAME", length = QuartzColumnLength.TRIGGER_NAME)
  private String triggerName;

  @Column(name = "TRIGGER_GROUP", length = QuartzColumnLength.TRIGGER_GROUP)
  private String triggerGroup;

}
