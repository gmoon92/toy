package com.gmoon.springpoi.excels.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;

class ExcelUploadTaskTest {

	private ExcelUploadTask newUserTask(int totalRows, int chunkSize, Locale locale) {
		return new ExcelUploadTask(ExcelSheetType.USER, totalRows, chunkSize)
			 .withRequesterLocale(locale, "Asia/Seoul")
			 .withFilename("user.xlsx");
	}

	@DisplayName("Task 생성시 필드/Task 초기화 상태 확인")
	@Test
	void newTask() {
		int totalRows = 103;
		int chunkSize = 20;
		Locale locale = Locale.KOREA;

		ExcelUploadTask task = newUserTask(totalRows, chunkSize, locale);

		assertThat(task.getSignature()).as("Signature 값 검증").isEqualTo(ExcelSheetType.USER.getSignature());
		assertThat(task.getSignature()).as("Signature 해시 길이는 %d.", ColumnLength.SHA_256).hasSize(ColumnLength.SHA_256);

		assertThat(task.getSheetType()).as("SheetType 값 검증").isEqualTo(ExcelSheetType.USER);
		assertThat(task.getStatus()).as("초기 상태는 PREPARE").isEqualTo(ExcelUploadTaskStatus.PREPARING);
		assertThat(task.getTotalRowCount()).as("총 행 수 검증").isEqualTo(totalRows);
		assertThat(task.getProcessedRowCount()).as("초기 processedRows 값은 0이어야 함").isZero();
		assertThat(task.getInvalidRowCount()).as("초기 invalidRows 값은 0이어야 함").isZero();
		assertThat(task.getCreatedAt()).as("생성일자 null 검증").isNotNull();

		assertThat(task.getChunks())
			 .as("Task 리스트가 empty가 아닌지 검증").isNotEmpty()
			 .as("청크 개수 검증").hasSize((int)Math.ceil(totalRows / (double)chunkSize));
	}

	@DisplayName("row가 chunkSize 배수가 아닌 경우에도 chunk 분할이 정확히 동작한다.")
	@Test
	void chunkTask() {
		ExcelSheetType sheetType = ExcelSheetType.USER;
		int totalRows = 99;
		int chunkSize = 20;
		ExcelUploadTask task = new ExcelUploadTask(sheetType, totalRows, chunkSize)
			 .withRequesterLocale(Locale.KOREA, "Asia/Seoul")
			 .withFilename("2025_인사팀.xlsx");

		assertThat(task.getChunks())
			 .as("Chunk 개수 검증").hasSize(5)
			 .satisfiesExactly(
				  task0 -> {
					  assertThat(task0.getTask()).isEqualTo(task);
					  assertThat(task0.getStatus()).as("초기 상태는 STARTING").isEqualTo(ExcelUploadChunkStatus.PREPARING);
					  assertThat(task0.getStartIdx()).as("Task startRow 계산 검증").isEqualTo(0);
					  assertThat(task0.getEndIdx()).as("Task endRow 계산 검증").isEqualTo(20);
					  assertThat(task0.getTotalRowCount()).as("Task row 수 = chunkSize 인지 검증").isEqualTo(20);
				  },
				  task1 -> {
					  assertThat(task1.getTask()).isEqualTo(task);
					  assertThat(task1.getStatus()).isEqualTo(ExcelUploadChunkStatus.PREPARING);
					  assertThat(task1.getStartIdx()).isEqualTo(20);
					  assertThat(task1.getEndIdx()).isEqualTo(40);
					  assertThat(task1.getTotalRowCount()).isEqualTo(20);
				  },
				  task2 -> {
					  assertThat(task2.getTask()).isEqualTo(task);
					  assertThat(task2.getStatus()).isEqualTo(ExcelUploadChunkStatus.PREPARING);
					  assertThat(task2.getStartIdx()).isEqualTo(40);
					  assertThat(task2.getEndIdx()).isEqualTo(60);
					  assertThat(task2.getTotalRowCount()).isEqualTo(20);
				  },
				  task3 -> {
					  assertThat(task3.getTask()).isEqualTo(task);
					  assertThat(task3.getStatus()).isEqualTo(ExcelUploadChunkStatus.PREPARING);
					  assertThat(task3.getStartIdx()).isEqualTo(60);
					  assertThat(task3.getEndIdx()).isEqualTo(80);
					  assertThat(task3.getTotalRowCount()).isEqualTo(20);
				  },
				  task4 -> {
					  assertThat(task4.getTask()).isEqualTo(task);
					  assertThat(task4.getStatus()).isEqualTo(ExcelUploadChunkStatus.PREPARING);
					  assertThat(task4.getStartIdx()).isEqualTo(80);
					  assertThat(task4.getEndIdx()).isEqualTo(99);
					  assertThat(task4.getTotalRowCount()).isEqualTo(19);
				  }
			 );
	}

	@DisplayName("기본 생성/청크 분할/구간 및 signature 검증")
	@Test
	void test() {
		int dataRowCount = 1000;
		int chunkSize = 500;
		ExcelUploadTask task = newUserTask(dataRowCount, chunkSize, Locale.KOREA);

		assertThat(task.getSheetType()).isEqualTo(ExcelSheetType.USER);
		assertThat(task.getStatus()).isEqualTo(ExcelUploadTaskStatus.PREPARING);
		assertThat(task.getSignature()).hasSize(ColumnLength.SHA_256);

		List<ExcelUploadTaskChunk> chunks = task.getChunks();
		assertThat(chunks)
			 .isNotEmpty()
			 .hasSize((dataRowCount + chunkSize - 1) / chunkSize);

		// 각 청크 start/end 및 연관값 검증, 마지막 청크의 endIdx 검증
		for (int i = 0; i < chunks.size(); i++) {
			ExcelUploadTaskChunk chunk = chunks.get(i);
			int expectedStartIdx = i * chunkSize;
			int expectedEndIdx = Math.min((i + 1) * chunkSize, dataRowCount);
			assertThat(chunk.getStartIdx()).isEqualTo(expectedStartIdx);
			assertThat(chunk.getEndIdx()).isEqualTo(expectedEndIdx);
			assertThat(chunk.getAttemptCount()).isEqualTo(0);
			assertThat(chunk.getSheetType()).isEqualTo(ExcelSheetType.USER);
			assertThat(chunk.getTask()).isEqualTo(task);
		}
		assertThat(chunks.getLast().getEndIdx()).isEqualTo(dataRowCount);
	}

	@DisplayName("상태 전이/예외 등 기존 로직 연속 검증")
	@Test
	void startProcessingTransition() {
		ExcelUploadTask task = newUserTask(500, 100, Locale.KOREA);

		assertThat(task.getStatus())
			 .as("파싱 전 준비(행수 제한 검증, 파일 저장 등) 상태를 가진다.")
			 .isEqualTo(ExcelUploadTaskStatus.PREPARING);

		task.startProcessing();
		assertThat(task.getStatus())
			 .as("엑셀 파싱이 시작되면 엑셀 파싱 중 상태로 변경된다.")
			 .isEqualTo(ExcelUploadTaskStatus.PROCESSING);

		task.mergeChunkResults();
		assertThat(task.getStatus())
			 .as("모든 청크들이 완료되지 않으면 상태가 변경되지 않는다.")
			 .isEqualTo(ExcelUploadTaskStatus.PROCESSING);

		task.getChunks().forEach(chunk -> chunk.complete(0));
		task.mergeChunkResults();
		assertThat(task.getStatus())
			 .as("모든 청크들이 성공됐다면 성공 상태로 변경된다.")
			 .isEqualTo(ExcelUploadTaskStatus.SUCCESS);

		ExcelUploadTask failTask = newUserTask(500, 100, Locale.KOREA);
		failTask.startProcessing();
		failTask.getChunks().forEach(chunk -> {
			chunk.fail("시스템 예외 발생!");
			chunk.complete(0);
		});
		failTask.mergeChunkResults();
		assertThat(failTask.getStatus())
			 .as("청크 하나가 실패되면 업로드 상태도 실패로 된다.")
			 .isEqualTo(ExcelUploadTaskStatus.FAILED);
	}

	@DisplayName(
		 "chunkSize > dataRowCount일 경우 단일 청크 생성, "
			  + "마지막 chunk의 endIdx는 항상 dataRowCount와 같음"
	)
	@Test
	void chunk() {
		int dataRowCount = 1234;
		int chunkSize = 500;
		ExcelUploadTask task = newUserTask(dataRowCount, chunkSize, Locale.KOREA);

		List<ExcelUploadTaskChunk> chunks = task.getChunks();
		ExcelUploadTaskChunk chunk = chunks.getFirst();
		assertThat(chunk.getStartIdx()).isEqualTo(0);
		assertThat(chunk.getEndIdx()).isEqualTo(chunkSize);

		ExcelUploadTaskChunk lastChunk = chunks.getLast();
		assertThat(lastChunk.getEndIdx())
			 .as("마지막 chunk의 endIdx는 dataRowCount와 같아야 한다.")
			 .isEqualTo(dataRowCount);
	}

	@DisplayName("chunkSize 변경에 따라 chunk 개수/구간도 변동")
	@Test
	void changeChunkSizeChangeChunkPartitioning() {
		int dataRowCount = 1000;

		ExcelUploadTask task200 = newUserTask(dataRowCount, 200, Locale.KOREA);
		ExcelUploadTask task500 = newUserTask(dataRowCount, 500, Locale.KOREA);
		ExcelUploadTask task1000 = newUserTask(dataRowCount, 1000, Locale.KOREA);

		// 200
		assertThat(task200.getChunks()).hasSize(5);
		assertThat(task200.getChunks().get(4).getStartIdx()).isEqualTo(800);
		assertThat(task200.getChunks().get(4).getEndIdx()).isEqualTo(1000);
		ExcelUploadTaskChunk lastChunk200 = task200.getChunks().get(4);
		assertThat(lastChunk200.getStartIdx()).isEqualTo(800);
		assertThat(lastChunk200.getEndIdx()).isEqualTo(1000);

		// 500
		assertThat(task500.getChunks()).hasSize(2);
		assertThat(task500.getChunks().get(1).getStartIdx()).isEqualTo(500);
		assertThat(task500.getChunks().get(1).getEndIdx()).isEqualTo(1000);
		ExcelUploadTaskChunk lastChunk500 = task500.getChunks().get(1);
		assertThat(lastChunk500.getStartIdx()).isEqualTo(500);
		assertThat(lastChunk500.getEndIdx()).isEqualTo(1000);

		// 1000
		assertThat(task1000.getChunks())
			 .as("chunkSize가 dataRowCount보다 크면 청크는 항상 1개만 생김")
			 .hasSize(1);
		assertThat(task1000.getChunks().getFirst().getStartIdx()).isEqualTo(0);
		assertThat(task1000.getChunks().getFirst().getEndIdx()).isEqualTo(dataRowCount);
	}

	@Test
	void getLocale() {
		List<Locale> locales = Arrays.asList(
			 Locale.KOREA,
			 Locale.CANADA,
			 Locale.JAPAN,
			 Locale.UK,
			 Locale.US
		);

		for (Locale locale : locales) {
			ExcelUploadTask task = newUserTask(1000, 500, locale);

			assertThat(locale).isEqualTo(task.getLocale());
		}
	}
}
