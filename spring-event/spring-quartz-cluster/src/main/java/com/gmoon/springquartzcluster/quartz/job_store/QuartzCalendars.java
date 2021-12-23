package com.gmoon.springquartzcluster.quartz.job_store;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springquartzcluster.quartz.job_store.id.QuartzId;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "QUARTZ_CALENDARS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzCalendars {
	@EmbeddedId
	private Id id;

	@Lob
	@Column(name = "CALENDAR", columnDefinition = "BLOB")
	private byte[] calendar;

	@Embeddable
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	protected static class Id extends QuartzId {

		@Column(name = "CALENDAR_NAME", length = QuartzColumnLength.CALENDAR_NAME)
		private String calendarName;
	}
}
