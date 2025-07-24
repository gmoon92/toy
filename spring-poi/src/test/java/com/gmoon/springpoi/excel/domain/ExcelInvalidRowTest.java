package com.gmoon.springpoi.excel.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gmoon.springpoi.base.persistence.vo.JsonString;

class ExcelInvalidRowTest {

	@DisplayName("생성자: 업로드 요청 객체만으로 생성 가능 및 초기화 값 검증")
	@Test
	void constructor() {
		ExcelUploadRequest uploadRequest = new ExcelUploadRequest();

		ExcelInvalidRow invalidRow = new ExcelInvalidRow(uploadRequest);

		assertThat(invalidRow.getExcelUploadRequest()).isSameAs(uploadRequest);
		assertThat(invalidRow.getOriginCellValues())
			 .as("원본 입력값은 비어있는 JsonString으로 초기화된다")
			 .isNotNull()
			 .extracting(JsonString::isEmpty)
			 .isEqualTo(true);
		assertThat(invalidRow.getInvalidColumnIndexes())
			 .as("유효성 실패 컬럼 인덱스는 비어있는 Set으로 초기화된다")
			 .isEmpty();
		assertThat(invalidRow.getType()).isEqualTo(ExcelInvalidRowType.VALIDATION);
	}

	@DisplayName("원본 입력값(Map) 저장 시 JsonString으로 변환된다")
	@Test
	void setOriginCellValues() {
		ExcelInvalidRow invalidRow = new ExcelInvalidRow(new ExcelUploadRequest());
		Map<String, String> cellValues = new HashMap<>();
		cellValues.put("1", "홍길동");
		cellValues.put("2", "010-1234-5678");

		invalidRow.setOriginCellValues(cellValues);

		JsonString origin = invalidRow.getOriginCellValues();
		assertThat(origin.get("1")).isEqualTo("홍길동");
		assertThat(origin.get("2")).isEqualTo("010-1234-5678");
	}

	@DisplayName("유효성 실패 컬럼 인덱스 추가 동작")
	@Test
	void addInvalidColumnIndex() {
		ExcelInvalidRow invalidRow = new ExcelInvalidRow(new ExcelUploadRequest());

		invalidRow.addInvalidCellColIndex(2);
		invalidRow.addInvalidCellColIndex(5);
		invalidRow.addInvalidCellColIndex(2);

		assertThat(invalidRow.getInvalidColumnIndexes())
			 .containsExactlyInAnyOrder(2, 5)
			 .hasSize(2);
	}
}
