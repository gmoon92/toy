package com.gmoon.springschedulingquartz.quartz;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuartzSchedulerHistoryRepository extends JpaRepository<QuartzSchedulerHistory, Long> {
}
