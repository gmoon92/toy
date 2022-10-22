package com.gmoon.hibernateannotation.payments;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.hibernateannotation.base.BaseRepositoryTest;
import com.gmoon.hibernateannotation.payments.price.domain.Currency;
import com.gmoon.hibernateannotation.payments.product.domain.CompanyProduct;
import com.gmoon.hibernateannotation.payments.product.domain.CompanyProductRepository;
import com.gmoon.hibernateannotation.payments.product.domain.vo.ProductType;

class CompanyProductRepositoryTest extends BaseRepositoryTest {

	@Autowired
	CompanyProductRepository repository;

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
