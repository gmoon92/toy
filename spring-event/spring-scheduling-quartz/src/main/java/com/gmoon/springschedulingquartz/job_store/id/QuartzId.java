package com.gmoon.springschedulingquartz.job_store.id;

import com.gmoon.springschedulingquartz.job_store.constants.QuartzColumnLength;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Getter
@MappedSuperclass
public abstract class QuartzId implements Serializable {

  @Column(name = "SCHED_NAME", length = QuartzColumnLength.SCHEDULER_NAME)
  private String schedulerName;

}
