package com.gmoon.springpoi.excels.application;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springpoi.common.excel.exception.ExcelEmptyDataRowException;
import com.gmoon.springpoi.common.excel.exception.NotFoundExcelFileException;
import com.gmoon.springpoi.common.excel.exception.SaxReadRangeOverflowException;
import com.gmoon.springpoi.common.exception.InvalidFileException;
import com.gmoon.springpoi.excels.domain.ExcelSheetType;
import com.gmoon.springpoi.excels.domain.ExcelUploadTask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

			ExcelUploadTask task = service.upload(excelFile, ExcelSheetType.USER, Locale.KOREA, "Asia/Seoul");

			assertThat(task.getSheetType()).isEqualTo(ExcelSheetType.USER);
			assertThat(task.getChunks()).isNotEmpty();
		}

		@DisplayName("빈 파일 케이스")
		@Test
		void uploadEmptyFile() {
			File emptyFile = getUploadedFile("excel-user-empty.xlsx");

			assertThatThrownBy(() -> service.upload(emptyFile, ExcelSheetType.USER, Locale.KOREA, "Asia/Seoul"))
				 .isInstanceOf(ExcelEmptyDataRowException.class);
		}

		@DisplayName("존재하지 않는 파일")
		@Test
		void uploadNonExistingFile() {
			File file = getUploadedFile("not-exist.xlsx");

			assertThatThrownBy(() -> service.upload(file, ExcelSheetType.USER, Locale.KOREA, "Asia/Seoul"))
				 .isInstanceOf(NotFoundExcelFileException.class);
		}

		@DisplayName("확장자 문제")
		@Test
		void uploadNotExcelFile() {
			File notExcel = getUploadedFile("fake.txt");

			assertThatThrownBy(() -> service.upload(notExcel, ExcelSheetType.USER, Locale.KOREA, "Asia/Seoul"))
				 .isInstanceOf(InvalidFileException.class);
		}

		@DisplayName("깨진 파일")
		@Test
		void uploadCorruptedFile() {
			File corrupted = getUploadedFile("corrupted.xlsx");

			assertThatThrownBy(() -> service.upload(corrupted, ExcelSheetType.USER, Locale.KOREA, "Asia/Seoul"))
				 .isInstanceOf(InvalidFileException.class);
		}

		@DisplayName("행 수 초과 파일(정책상 maxRows 지원시)")
		@Test
		void uploadFileWithTooManyRows() {
			File file = getUploadedFile("excel-user-5000.xlsx");

			assertThatThrownBy(() -> service.upload(file, ExcelSheetType.USER, Locale.KOREA, "Asia/Seoul"))
				 .isInstanceOf(SaxReadRangeOverflowException.class);
		}
	}

	@ParameterizedTest
	@ValueSource(ints = {
		 100,
		 // 500, 1000, 1500, 2000,
		 // 100, 500, 1000, 1500, 2000,
		 // 2500, 3500, 4000, 4500, 5000,
		 // 5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000,
		 // 15000, 20000, 25000, 30000,
		 // 35000, 40000, 45000, 50000,
	})
	void processExcelChunks(int dataRowCount) {
		String filename = "excel-user-" + dataRowCount + ".xlsx";
		Path excelFilePath = Paths.get("src/test/resources/sample/", filename);
		File file = excelFilePath.toFile();

		ExcelUploadTask task = processing(file, dataRowCount);

		assertThat(task.getInvalidRowCount()).isZero();
	}

	private ExcelUploadTask processing(File file, int dataRowCount) {
		ExcelUploadTask task = service.upload(
			 file,
			 ExcelSheetType.USER,
			 Locale.KOREA,
			 "Asia/Seoul"
		);

		CompletableFuture<Void> futures = CompletableFuture.allOf(
			 task.getChunks()
				  .stream()
				  .map(chunk ->
					   CompletableFuture.runAsync(() -> {
								service.startProcessing(task.getId(), chunk.getId());

								service.processing(chunk.getId());

								ExcelUploadTask result = service.updateTaskSummary(chunk.getId());
								log.info(
									 "update summary task status: {}(completed {}), processed rows: {}, invalid rows: {}.",
									 result.getStatus(),
									 result.isCompleted(),
									 result.getProcessedRowCount(),
									 result.getInvalidRowCount()
								);
							}
					   )
				  )
				  .toArray(CompletableFuture[]::new)
		);
		futures.join();

		ExcelUploadTask result = service.getTask(task.getId());
		log.info("task completed successfully.: {}, {}, {}",
			 result.getId(),
			 result.getProcessedRowCount(),
			 result.getInvalidRowCount()
		);
		assertThat(result).isNotNull();
		assertThat(result.getTotalRowCount()).isEqualTo(dataRowCount);
		assertThat(result.getTotalRowCount()).isEqualTo(result.getProcessedRowCount() + result.getInvalidRowCount());
		assertThat(result.getProcessedRowCount()).isGreaterThanOrEqualTo(0);
		assertThat(result.getInvalidRowCount()).isGreaterThanOrEqualTo(0);
		assertThat(result.isCompleted()).isTrue();
		return result;
	}
}
