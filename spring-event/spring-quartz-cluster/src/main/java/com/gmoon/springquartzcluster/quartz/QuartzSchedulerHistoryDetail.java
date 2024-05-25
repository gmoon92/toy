package com.gmoon.springquartzcluster.quartz;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
