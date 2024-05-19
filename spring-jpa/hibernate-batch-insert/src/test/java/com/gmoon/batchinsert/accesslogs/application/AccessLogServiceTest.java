package com.gmoon.batchinsert.accesslogs.application;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.batchinsert.global.prop.StorageProperties;

@SpringBootTest
class AccessLogServiceTest {

	@Autowired
	private AccessLogService service;

	@Autowired
	private StorageProperties storageProperties;

	@Disabled
	@Test
	void createExcelFile() {
		String filePath = storageProperties.getAbsolutePath() + "/test.xlsx";
		LocalDate attemptDt = LocalDate.of(2022, 1, 1);

		assertThatCode(() -> service.createExcelFile(filePath, attemptDt))
			 .doesNotThrowAnyException();
	}
}
