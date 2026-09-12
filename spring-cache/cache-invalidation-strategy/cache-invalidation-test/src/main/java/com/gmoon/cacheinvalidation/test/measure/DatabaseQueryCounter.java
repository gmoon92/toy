package com.gmoon.cacheinvalidation.test.measure;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import jakarta.persistence.EntityManagerFactory;

public class DatabaseQueryCounter {

	private final Statistics statistics;

	public DatabaseQueryCounter(EntityManagerFactory entityManagerFactory) {
		this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
		this.statistics.setStatisticsEnabled(true);
	}

	public long executedStatementCount() {
		return statistics.getPrepareStatementCount();
	}

	public void reset() {
		statistics.clear();
	}
}
