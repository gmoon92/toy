package com.gmoon.springschedulingquartz.job_store;

import com.gmoon.springschedulingquartz.job_store.constants.QuartzColumnLength;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;

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
  @ToString
  @EqualsAndHashCode
  private static class Id implements Serializable {
    @Column(name = "SCHED_NAME", length = QuartzColumnLength.SCHEDULER_NAME)
    private String schedulerName;

    @Column(name = "CALENDAR_NAME", length = QuartzColumnLength.CALENDAR_NAME)
    private String calendarName;
  }
}
