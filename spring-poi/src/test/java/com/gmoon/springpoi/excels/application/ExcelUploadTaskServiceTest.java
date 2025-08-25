package com.gmoon.springpoi.excels.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springpoi.excels.domain.ExcelSheetType;

@SpringBootTest
class ExcelUploadTaskServiceTest {

	@Autowired
	private ExcelUploadTaskService service;

	@Test
	void save() {
		ExcelSheetType sheetType = ExcelSheetType.USER;
		long dataRowCount = 1_000;
		String filename = "user.xlsx";

		assertThatCode(() -> service.save(sheetType, dataRowCount, filename, Locale.KOREA, "Asia/Seoul"))
			 .doesNotThrowAnyException();
	}
}
