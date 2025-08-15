package com.gmoon.springpoi.excels.application;

import static org.assertj.core.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springpoi.common.excel.exception.ExcelEmptyDataRowException;
import com.gmoon.springpoi.common.excel.exception.NotFoundExcelFileException;
import com.gmoon.springpoi.common.excel.exception.SaxReadRangeOverflowException;
import com.gmoon.springpoi.common.exception.InvalidFileException;
import com.gmoon.springpoi.excels.domain.ExcelSheetType;
import com.gmoon.springpoi.excels.domain.ExcelUploadJob;

@SpringBootTest
class ExcelUploadServiceTest {

	@Autowired
	private ExcelUploadService service;

	@Nested
	class UploadTest {

		private File getUploadedFile(String filename) {
			return new File("src/test/resources/sample/" + filename);
		}

		@Test
		void success() {
			int dataSize = 100;
			String filename = "excel-user-" + dataSize + ".xlsx";
			File excelFile = getUploadedFile(filename);

			ExcelUploadJob upload = service.upload(excelFile, ExcelSheetType.USER);

			assertThat(upload.getSheetType()).isEqualTo(ExcelSheetType.USER);
			assertThat(upload.getTasks()).isNotEmpty();
		}

		@DisplayName("빈 파일 케이스")
		@Test
		void uploadEmptyFile() {
			File emptyFile = getUploadedFile("excel-user-empty.xlsx");

			assertThatThrownBy(() -> service.upload(emptyFile, ExcelSheetType.USER))
				 .isInstanceOf(ExcelEmptyDataRowException.class);
		}

		@DisplayName("존재하지 않는 파일")
		@Test
		void uploadNonExistingFile() {
			File file = getUploadedFile("not-exist.xlsx");

			assertThatThrownBy(() -> service.upload(file, ExcelSheetType.USER))
				 .isInstanceOf(NotFoundExcelFileException.class);
		}

		@DisplayName("확장자 문제")
		@Test
		void uploadNotExcelFile() {
			File notExcel = getUploadedFile("fake.txt");

			assertThatThrownBy(() -> service.upload(notExcel, ExcelSheetType.USER))
				 .isInstanceOf(InvalidFileException.class);
		}

		@DisplayName("깨진 파일")
		@Test
		void uploadCorruptedFile() {
			File corrupted = getUploadedFile("corrupted.xlsx");

			assertThatThrownBy(() -> service.upload(corrupted, ExcelSheetType.USER))
				 .isInstanceOf(InvalidFileException.class);
		}

		@DisplayName("행 수 초과 파일(정책상 maxRows 지원시)")
		@Test
		void uploadFileWithTooManyRows() {
			File file = getUploadedFile("excel-user-5000.xlsx");

			assertThatThrownBy(() -> service.upload(file, ExcelSheetType.USER))
				 .isInstanceOf(SaxReadRangeOverflowException.class);
		}
	}
}
