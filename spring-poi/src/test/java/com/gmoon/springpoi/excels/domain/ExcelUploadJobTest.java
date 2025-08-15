package com.gmoon.springpoi.excels.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;

class ExcelUploadJobTest {

	@DisplayName("Job 생성시 필드/Task 초기화 상태 확인")
	@Test
	void createJob() {
		ExcelSheetType sheetType = ExcelSheetType.USER;
		int totalRows = 103;
		int chunkSize = 20;

		ExcelUploadJob job = new ExcelUploadJob(sheetType, 103, 20);

		assertThat(job.getSignature()).as("Signature 값 검증").isEqualTo(sheetType.signature);
		assertThat(job.getSignature()).as("Signature 해시 길이는 %d.", ColumnLength.SHA_256).hasSize(ColumnLength.SHA_256);

		assertThat(job.getSheetType()).as("SheetType 값 검증").isEqualTo(sheetType);
		assertThat(job.getStatus()).as("초기 상태는 PREPARE").isEqualTo(ExcelUploadJobStatus.PREPARE);
		assertThat(job.getTotalRows()).as("총 행 수 검증").isEqualTo(totalRows);
		assertThat(job.getProcessedRows()).as("초기 processedRows 값은 0이어야 함").isZero();
		assertThat(job.getInvalidRows()).as("초기 invalidRows 값은 0이어야 함").isZero();
		assertThat(job.getCreatedAt()).as("생성일자 null 검증").isNotNull();

		assertThat(job.getTasks())
			 .as("Task 리스트가 empty가 아닌지 검증").isNotEmpty()
			 .as("청크 개수 검증").hasSize((int)Math.ceil(totalRows / (double)chunkSize));
	}

	@DisplayName("row가 chunkSize 배수가 아닌 경우에도 chunk 분할이 정확히 동작한다.")
	@Test
	void chunkTask() {
		ExcelSheetType sheetType = ExcelSheetType.USER;
		int totalRows = 99;
		int chunkSize = 20;

		ExcelUploadJob job = new ExcelUploadJob(sheetType, totalRows, chunkSize);

		assertThat(job.getTasks())
			 .as("Chunk 개수 검증").hasSize(5)
			 .satisfiesExactly(
				  task0 -> {
					  assertThat(task0.getJob()).isEqualTo(job);
					  assertThat(task0.getStatus()).as("초기 상태는 STARTING").isEqualTo(ExcelUploadTaskStatus.STARTING);
					  assertThat(task0.getStartRow()).as("Task startRow 계산 검증").isEqualTo(0);
					  assertThat(task0.getEndRow()).as("Task endRow 계산 검증").isEqualTo(20);
					  assertThat(task0.getTotalRows()).as("Task row 수 = chunkSize 인지 검증").isEqualTo(20);
				  },
				  task1 -> {
					  assertThat(task1.getJob()).isEqualTo(job);
					  assertThat(task1.getStatus()).isEqualTo(ExcelUploadTaskStatus.STARTING);
					  assertThat(task1.getStartRow()).isEqualTo(20);
					  assertThat(task1.getEndRow()).isEqualTo(40);
					  assertThat(task1.getTotalRows()).isEqualTo(20);
				  },
				  task2 -> {
					  assertThat(task2.getJob()).isEqualTo(job);
					  assertThat(task2.getStatus()).isEqualTo(ExcelUploadTaskStatus.STARTING);
					  assertThat(task2.getStartRow()).isEqualTo(40);
					  assertThat(task2.getEndRow()).isEqualTo(60);
					  assertThat(task2.getTotalRows()).isEqualTo(20);
				  },
				  task3 -> {
					  assertThat(task3.getJob()).isEqualTo(job);
					  assertThat(task3.getStatus()).isEqualTo(ExcelUploadTaskStatus.STARTING);
					  assertThat(task3.getStartRow()).isEqualTo(60);
					  assertThat(task3.getEndRow()).isEqualTo(80);
					  assertThat(task3.getTotalRows()).isEqualTo(20);
				  },
				  task4 -> {
					  assertThat(task4.getJob()).isEqualTo(job);
					  assertThat(task4.getStatus()).isEqualTo(ExcelUploadTaskStatus.STARTING);
					  assertThat(task4.getStartRow()).isEqualTo(80);
					  assertThat(task4.getEndRow()).isEqualTo(99);
					  assertThat(task4.getTotalRows()).isEqualTo(19);
				  }
			 );
	}
}
