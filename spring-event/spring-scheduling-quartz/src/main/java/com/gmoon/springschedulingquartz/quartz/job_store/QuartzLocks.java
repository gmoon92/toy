package com.gmoon.springschedulingquartz.quartz.job_store;

import com.gmoon.springschedulingquartz.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springschedulingquartz.quartz.job_store.id.QuartzId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "QUARTZ_LOCKS")
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzLocks {

  @EmbeddedId
  private Id id;

  @Embeddable
  @ToString(callSuper = true)
  @EqualsAndHashCode(callSuper = true)
  private static class Id extends QuartzId {

    @Column(name = "LOCK_NAME", length = QuartzColumnLength.LOCK_NAME)
    private String lockName;
  }
}
