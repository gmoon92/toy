package com.gmoon.batchinsert.accesslogs.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, String> {

	List<AccessLog> findAllByUsername(String username);
	List<AccessLog> findAllByAttemptDtBetween(LocalDateTime from, LocalDateTime to);
}
