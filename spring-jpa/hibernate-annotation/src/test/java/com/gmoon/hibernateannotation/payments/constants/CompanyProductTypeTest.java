package com.gmoon.hibernateannotation.payments.constants;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.gmoon.hibernateannotation.payments.product.domain.vo.ProductType;

class CompanyProductTypeTest {

	@Test
	void testIsUpgradeAndDownGrade() {
		// given
		ProductType productType = ProductType.BUSINESS;

		// when then
		assertThat(productType.isUpgrade(ProductType.FREE)).isFalse();
		assertThat(productType.isUpgrade(ProductType.BUSINESS)).isFalse();
		assertThat(productType.isUpgrade(ProductType.STANDARD)).isTrue();
		assertThat(productType.isUpgrade(ProductType.ENTERPRISE)).isTrue();

		assertThat(productType.isDowngrade(ProductType.FREE)).isTrue();
		assertThat(productType.isDowngrade(ProductType.BUSINESS)).isFalse();
		assertThat(productType.isDowngrade(ProductType.STANDARD)).isFalse();
		assertThat(productType.isDowngrade(ProductType.ENTERPRISE)).isFalse();
	}
}
