package com.gmoon.springpoi.excels.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gmoon.springpoi.excels.domain.vo.ExcelCellErrorMessages;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

class ExcelUploadInvalidRowTest {

	@DisplayName("생성자: 업로드 요청 객체만으로 생성 가능 및 초기화 값 검증")
	@Test
	void constructor() {
		ExcelUploadInvalidRow invalidRow = new ExcelUploadInvalidRow(newChunk());

		assertThat(invalidRow.getOriginCellValues())
			 .as("원본 입력값은 비어있는 ExcelCellValues으로 초기화된다")
			 .isNotNull()
			 .extracting(ExcelCellValues::isEmpty)
			 .isEqualTo(true);
		assertThat(invalidRow.getErrorMessages().isEmpty())
			 .as("유효성 실패 메시지는 비어있는 Map으로 초기화된다")
			 .isTrue();
		assertThat(invalidRow.getType()).isEqualTo(ExcelInvalidRowType.VALIDATION);
	}

	@DisplayName("원본 입력값(Map) 저장 시 ExcelCellValues으로 변환된다")
	@Test
	void markAsInvalid() {
		ExcelUploadInvalidRow invalidRow = new ExcelUploadInvalidRow(newChunk());

		Map<Integer, String> cellValues = new HashMap<>();
		cellValues.put(1, "홍길동");
		cellValues.put(2, "010-1234-5678");
		invalidRow.setInvalidCellValues(new ExcelCellValues(cellValues), new ExcelCellErrorMessages());

		assertThat(invalidRow.getType()).isEqualTo(ExcelInvalidRowType.VALIDATION);

		ExcelCellValues origin = invalidRow.getOriginCellValues();
		assertThat(origin.get(1)).isEqualTo("홍길동");
		assertThat(origin.get(2)).isEqualTo("010-1234-5678");
	}

	private ExcelUploadTaskChunk newChunk() {
		return new ExcelUploadTask(ExcelSheetType.USER, 1_000, 500)
			 .withFilename("2025_인사팀.xlsx")
			 .withRequesterLocale(Locale.KOREA, "Asia/Seoul")
			 .getChunks()
			 .getFirst();
	}
}
