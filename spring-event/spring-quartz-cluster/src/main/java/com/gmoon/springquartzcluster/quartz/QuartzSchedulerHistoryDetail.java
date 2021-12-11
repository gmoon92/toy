package com.gmoon.springquartzcluster.quartz;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzSchedulerHistoryDetail {
  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "scheduler_id", referencedColumnName = "id")
  private QuartzSchedulerHistory schedulerHistory;

  private String ip;

  public static QuartzSchedulerHistoryDetail create(QuartzSchedulerHistory schedulerHistory, String ip) {
    QuartzSchedulerHistoryDetail detail = new QuartzSchedulerHistoryDetail();
    detail.schedulerHistory = schedulerHistory;
    detail.ip = ip;
    return detail;
  }
}
