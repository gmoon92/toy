package com.gmoon.springquartzcluster.quartz;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@ToString(exclude = "details")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuartzSchedulerHistory {
	@Id
	@GeneratedValue
	private Long id;

	private String instanceId;

	@OneToMany(mappedBy = "schedulerHistory", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<QuartzSchedulerHistoryDetail> details = new ArrayList<>();

	private QuartzSchedulerHistory(String instanceId) {
		this.instanceId = instanceId;
	}

	public static QuartzSchedulerHistory from(String instanceId, List<String> ipAddresses) {
		QuartzSchedulerHistory history = new QuartzSchedulerHistory(instanceId);
		for (String ip : ipAddresses) {
			history.addHistoryDetail(ip);
		}
		return history;
	}

	private void addHistoryDetail(String ip) {
		details.add(QuartzSchedulerHistoryDetail.create(this, ip));
	}
}
