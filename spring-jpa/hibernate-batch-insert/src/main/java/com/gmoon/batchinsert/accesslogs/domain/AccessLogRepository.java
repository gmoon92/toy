package com.gmoon.batchinsert.accesslogs.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, String> {

	List<AccessLog> findAllByUsername(String username);

	List<AccessLog> findAllByAttemptAtBetween(Instant from, Instant to);
}
