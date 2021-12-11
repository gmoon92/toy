package com.gmoon.springquartzcluster.quartz;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuartzSchedulerHistoryRepository extends JpaRepository<QuartzSchedulerHistory, Long> {
}
