package com.gmoon.localstack.aws.constants;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AmazonRegionTest {

	@ParameterizedTest
	@EnumSource(AmazonRegion.class)
	void getValue(AmazonRegion region) {
		// given
		String actual = region.getValue();

		// when then
		assertThat(AmazonRegion.ALL.get(actual))
			 .isEqualTo(region);
	}
}
