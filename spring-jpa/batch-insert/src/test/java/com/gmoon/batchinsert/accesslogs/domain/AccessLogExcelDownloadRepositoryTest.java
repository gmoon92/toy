package com.gmoon.batchinsert.accesslogs.domain;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccessLogExcelDownloadRepositoryTest {

	private static List<AccessLog> accessLogs;

	@Autowired
	private AccessLogExcelDownloadRepository repository;


	@BeforeAll
	static void beforeAll(@Autowired AccessLogRepository accessLogRepository) {
		accessLogs = accessLogRepository.findAll();
	}

	@DisplayName("JPA 2 sec")
	@Test
	void saveAll() {
		List<AccessLogExcelDownload> registered = accessLogs.stream()
			.map(AccessLogExcelDownload::create)
			.collect(Collectors.toList());

		repository.saveAll(registered);
	}

	@DisplayName("JOOQ 117m")
	@Test
	void bulkSaveAllAtJooq() {
		repository.bulkSaveAllAtJooq(accessLogs);
	}

	@DisplayName("StatelessSession 117m")
	@Test
	void bulkSaveAllAtStatelessSession() {
		repository.bulkSaveAllAtStatelessSession(accessLogs);
	}

	@AfterEach
	void tearDown() {
		repository.flush();
	}
}
