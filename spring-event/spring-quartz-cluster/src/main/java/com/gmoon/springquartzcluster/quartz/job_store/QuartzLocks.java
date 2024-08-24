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

@Entity
@Table(name = "QUARTZ_LOCKS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuartzLocks {
	@EmbeddedId
	@EqualsAndHashCode.Include
	private Id id;

	@Embeddable
	@EqualsAndHashCode(callSuper = true)
	protected static class Id extends QuartzId {

		@Column(name = "LOCK_NAME", length = QuartzColumnLength.LOCK_NAME)
		private String lockName;
	}
}
