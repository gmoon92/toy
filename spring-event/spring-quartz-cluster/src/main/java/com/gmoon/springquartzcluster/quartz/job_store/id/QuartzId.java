package com.gmoon.springquartzcluster.quartz.job_store.id;

import java.io.Serializable;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
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
