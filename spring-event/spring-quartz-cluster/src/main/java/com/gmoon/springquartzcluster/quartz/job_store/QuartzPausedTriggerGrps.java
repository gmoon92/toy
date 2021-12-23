package com.gmoon.springquartzcluster.quartz.job_store;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springquartzcluster.quartz.job_store.id.QuartzId;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "QUARTZ_PAUSED_TRIGGER_GRPS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzPausedTriggerGrps {
	@EmbeddedId
	private Id id;

	@Embeddable
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	protected static class Id extends QuartzId {

		@Column(name = "TRIGGER_GROUP", length = QuartzColumnLength.TRIGGER_GROUP)
		private String triggerGroup;
	}
}
