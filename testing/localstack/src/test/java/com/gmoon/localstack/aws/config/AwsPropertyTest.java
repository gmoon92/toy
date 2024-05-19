package com.gmoon.localstack.aws.config;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.localstack.aws.constants.AmazonRegion;

@SpringBootTest
class AwsPropertyTest {

	@Autowired
	AwsProperty property;

	@DisplayName("프로퍼티 값 검증")
	@Test
	void getValue() {
		assertAll(
			 () -> assertThat(property.s3.region).isEqualTo(AmazonRegion.AP_NORTHEAST_2),
			 () -> assertThat(property.s3.accessKey).isNotBlank(),
			 () -> assertThat(property.s3.secretKey).isNotBlank()
		);
	}
}
