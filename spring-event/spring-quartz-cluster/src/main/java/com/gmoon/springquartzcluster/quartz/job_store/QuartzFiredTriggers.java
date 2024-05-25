package com.gmoon.springquartzcluster.quartz.job_store;

import org.hibernate.annotations.ColumnDefault;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnValue;
import com.gmoon.springquartzcluster.quartz.job_store.id.QuartzId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "QUARTZ_FIRED_TRIGGERS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzFiredTriggers {
	@EmbeddedId
	private Id id;

	@Column(name = "TRIGGER_NAME", length = QuartzColumnLength.TRIGGER_NAME)
	private String triggerName;

	@Column(name = "TRIGGER_GROUP", length = QuartzColumnLength.TRIGGER_GROUP)
	private String triggerGroup;

	@Column(name = "INSTANCE_NAME", length = QuartzColumnLength.INSTANCE_NAME)
	private String instanceName;

	@Column(name = "FIRED_TIME", length = QuartzColumnLength.FIRED_TIME)
	private long firedTime;

	@Column(name = "SCHED_TIME", length = QuartzColumnLength.SCHED_TIME)
	private long schedulerTime;

	@Column(name = "PRIORITY")
	private int priority;

	@Column(name = "STATE", length = QuartzColumnLength.SATE)
	private String state;

	@Column(name = "JOB_NAME", length = QuartzColumnLength.JOB_NAME)
	private String jobName;

	@Column(name = "JOB_GROUP", length = QuartzColumnLength.JOB_GROUP)
	private String jobGroup;

	@ColumnDefault(QuartzColumnValue.ENABLED)
	@Column(name = "IS_NONCONCURRENT", length = QuartzColumnLength.IS_NONCONCURRENT)
	private String useNonConcurrent;

	@ColumnDefault(QuartzColumnValue.ENABLED)
	@Column(name = "REQUESTS_RECOVERY", length = QuartzColumnLength.REQUESTS_RECOVERY)
	private String useRequestsRecovery;

	@Embeddable
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	protected static class Id extends QuartzId {

		@Column(name = "ENTRY_ID", length = QuartzColumnLength.ENTRY_ID)
		private String entryId;
	}
}
