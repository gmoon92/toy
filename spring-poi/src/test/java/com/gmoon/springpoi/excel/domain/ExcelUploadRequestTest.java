package com.gmoon.springpoi.excel.domain;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;

class ExcelUploadRequestTest {

	@Test
	void test() {
		ExcelUploadRequest excelUploadRequest = new ExcelUploadRequest(ExcelSheetType.USER);

		assertThat(excelUploadRequest)
			 .extracting(ExcelUploadRequest::getSignature, ExcelUploadRequest::getCreatedAt)
			 .as("Signature 해시 길이와 생성일자가 올바르게 생성된다.")
			 .satisfies(signatureAndDate -> {
				 String signature = (String)signatureAndDate.get(0);
				 Instant createdAt = (Instant)signatureAndDate.get(1);
				 assertThat(signature).hasSize(ColumnLength.SHA_256);
				 assertThat(createdAt).isNotNull();
			 });
	}
}
