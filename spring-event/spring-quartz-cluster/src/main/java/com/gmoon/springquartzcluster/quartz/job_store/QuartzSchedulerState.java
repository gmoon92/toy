package com.gmoon.springquartzcluster.quartz.job_store;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;
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
@Table(name = "QUARTZ_SCHEDULER_STATE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuartzSchedulerState {
	@EmbeddedId
	@EqualsAndHashCode.Include
	private Id id;

	@Column(name = "LAST_CHECKIN_TIME", length = QuartzColumnLength.LAST_CHECKIN_TIME)
	private Long lastCheckinTime;

	@Column(name = "CHECKIN_INTERVAL", length = QuartzColumnLength.CHECKIN_INTERVAL)
	private Long checkinInterval;

	@Embeddable
	@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
	@ToString(callSuper = true)
	protected static class Id extends QuartzId {

		@Column(name = "INSTANCE_NAME", length = QuartzColumnLength.INSTANCE_NAME)
		@EqualsAndHashCode.Include
		private String instanceName;
	}
}
