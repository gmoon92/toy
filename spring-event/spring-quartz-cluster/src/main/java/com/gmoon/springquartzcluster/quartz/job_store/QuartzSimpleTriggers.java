package com.gmoon.springquartzcluster.quartz.job_store;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springquartzcluster.quartz.job_store.id.QuartzTriggerId;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "QUARTZ_SIMPLE_TRIGGERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuartzSimpleTriggers {
	@EmbeddedId
	@EqualsAndHashCode.Include
	private QuartzTriggerId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
		 @JoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME", insertable = false, updatable = false),
		 @JoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME", insertable = false, updatable = false),
		 @JoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP", insertable = false, updatable = false)
	})
	private QuartzTriggers triggers;

	@Column(name = "REPEAT_COUNT", length = QuartzColumnLength.REPEAT_COUNT)
	private Long repeatCount;

	@Column(name = "REPEAT_INTERVAL", length = QuartzColumnLength.REPEAT_INTERVAL)
	private Long repeatInterval;

	@Column(name = "TIMES_TRIGGERED", length = QuartzColumnLength.TIMES_TRIGGERED)
	private Long timesTriggered;
}
