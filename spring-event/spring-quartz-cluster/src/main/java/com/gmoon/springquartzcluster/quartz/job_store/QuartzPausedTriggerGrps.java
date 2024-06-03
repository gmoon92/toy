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
@Table(name = "QUARTZ_PAUSED_TRIGGER_GRPS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class QuartzPausedTriggerGrps {
	@EmbeddedId
	private Id id;

	@Embeddable
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	protected static class Id extends QuartzId {

		@Column(name = "TRIGGER_GROUP", length = QuartzColumnLength.TRIGGER_GROUP)
		private String triggerGroup;
	}
}
