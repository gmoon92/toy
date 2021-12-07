package com.gmoon.springschedulingquartz.quartz;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@ToString(exclude = "details")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzSchedulerHistory {

  @Id
  @GeneratedValue
  private Long id;

  @OneToMany(mappedBy = "schedulerHistory", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<QuartzSchedulerHistoryDetail> details = new ArrayList<>();

  public static QuartzSchedulerHistory from(List<String> ipAddresses) {
    QuartzSchedulerHistory history = new QuartzSchedulerHistory();
    for (String ip : ipAddresses) {
      history.addHistoryDetail(ip);
    }
    return history;
  }

  private void addHistoryDetail(String ip) {
    details.add(QuartzSchedulerHistoryDetail.create(this, ip));
  }
}
