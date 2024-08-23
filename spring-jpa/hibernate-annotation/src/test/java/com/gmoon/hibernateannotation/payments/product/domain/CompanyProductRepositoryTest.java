package com.gmoon.hibernateannotation.payments.product.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.hibernateannotation.base.BaseRepositoryTest;
import com.gmoon.hibernateannotation.payments.price.domain.Currency;
import com.gmoon.hibernateannotation.payments.product.domain.vo.ProductType;

class CompanyProductRepositoryTest extends BaseRepositoryTest {

	@Autowired
	private CompanyProductRepository repository;

	@Test
	void save() {
		ProductType productType = ProductType.ENTERPRISE;
		CompanyProduct companyProduct = CompanyProduct.create(productType, 10_000d, Currency.WON);

		assertThat(repository.save(companyProduct))
			 .isInstanceOf(CompanyProduct.class)
			 .hasFieldOrPropertyWithValue("name", productType.name())
			 .hasFieldOrPropertyWithValue("type", productType);
	}
}
