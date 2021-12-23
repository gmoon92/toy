package com.gmoon.springquartzcluster.quartz.job_store;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springquartzcluster.quartz.job_store.id.QuartzTriggerId;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "QRTZ_CRON_TRIGGERS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzCronTriggers {
	@EmbeddedId
	private QuartzTriggerId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME", insertable = false, updatable = false),
		@JoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME", insertable = false, updatable = false),
		@JoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP", insertable = false, updatable = false)
	})
	private QuartzTriggers triggers;

	@Column(name = "CRON_EXPRESSION", length = QuartzColumnLength.CRON_EXPRESSION)
	private String cronExpression;

	@Column(name = "TIME_ZONE_ID", length = QuartzColumnLength.TIME_ZONE_ID)
	private String timeZoneId;
}
