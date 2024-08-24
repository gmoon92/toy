package com.gmoon.springquartzcluster.quartz.job_store;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springquartzcluster.quartz.job_store.id.QuartzId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "QUARTZ_CALENDARS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuartzCalendars {
	@EmbeddedId
	@EqualsAndHashCode.Include
	private Id id;

	@Lob
	@Column(name = "CALENDAR", columnDefinition = "BLOB")
	private byte[] calendar;

	@Embeddable
	@EqualsAndHashCode(callSuper = true)
	protected static class Id extends QuartzId {

		@Column(name = "CALENDAR_NAME", length = QuartzColumnLength.CALENDAR_NAME)
		private String calendarName;
	}
}
