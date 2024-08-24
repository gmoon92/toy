package com.gmoon.springquartzcluster.quartz;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuartzSchedulerHistory {
	@Id
	@GeneratedValue
	@EqualsAndHashCode.Include
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
