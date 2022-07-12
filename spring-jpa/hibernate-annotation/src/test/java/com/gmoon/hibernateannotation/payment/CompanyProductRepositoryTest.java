package com.gmoon.hibernateannotation.payment;

import static org.assertj.core.api.Assertions.assertThat;
import com.gmoon.hibernateannotation.base.BaseRepositoryTest;
import com.gmoon.hibernateannotation.payment.constants.Currency;
import com.gmoon.hibernateannotation.payment.constants.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CompanyProductRepositoryTest extends BaseRepositoryTest {

	@Autowired
	ProductRepository repository;

	@Test
	void testSave() {
		// given
		ProductType productType = ProductType.ENTERPRISE;
		CompanyProduct companyProduct = CompanyProduct.create(productType, 10_000d, Currency.WON);

		// when then
		assertThat(repository.save(companyProduct))
			.isInstanceOf(CompanyProduct.class)
			.hasFieldOrPropertyWithValue("name", productType.name())
			.hasFieldOrPropertyWithValue("type", productType);
	}
}
