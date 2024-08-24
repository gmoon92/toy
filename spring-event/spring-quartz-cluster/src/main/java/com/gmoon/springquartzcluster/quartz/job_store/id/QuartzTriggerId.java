package com.gmoon.springquartzcluster.quartz.job_store.id;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
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
