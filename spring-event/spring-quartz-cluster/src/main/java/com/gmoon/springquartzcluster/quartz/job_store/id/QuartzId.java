package com.gmoon.springquartzcluster.quartz.job_store.id;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@MappedSuperclass
@EqualsAndHashCode
public abstract class QuartzId implements Serializable {
	@Column(name = "SCHED_NAME", length = QuartzColumnLength.SCHEDULER_NAME)
	protected String schedulerName;
}
