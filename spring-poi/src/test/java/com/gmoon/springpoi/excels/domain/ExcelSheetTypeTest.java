package com.gmoon.springpoi.excels.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

class ExcelSheetTypeTest {

	@RepeatedTest(10)
	void signature() {
		for (ExcelSheetType sheetType : ExcelSheetType.values()) {
			Assertions.assertThat(sheetType.signature)
				 .matches("[a-f0-9]{64}")
				 .hasSize(64);
		}
	}
}
