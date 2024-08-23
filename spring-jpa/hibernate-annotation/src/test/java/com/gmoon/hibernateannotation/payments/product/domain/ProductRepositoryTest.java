package com.gmoon.hibernateannotation.payments.product.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.hibernateannotation.base.BaseRepositoryTest;
import com.gmoon.hibernateannotation.payments.price.domain.Currency;
import com.gmoon.hibernateannotation.payments.product.domain.vo.ProductType;

class ProductRepositoryTest extends BaseRepositoryTest {

	@Autowired
	private ProductRepository repository;

	@BeforeAll
	static void beforeAll(@Autowired ProductRepository productRepository) {
		productRepository.save(CompanyProduct.create(ProductType.ENTERPRISE, 1_000d, Currency.WON));
		productRepository.save(UserProduct.create(ProductType.ENTERPRISE, 1_000d, Currency.WON));
	}

	@DisplayName("다향성 조회 쿼리")
	@Test
	void testPolymorphismSelectQuery() {
		// given
		ProductType type = ProductType.ENTERPRISE;

		// when then
		assertThat(repository.findFirstCompanyProductByType(type)).isInstanceOf(CompanyProduct.class);
		assertThat(repository.findFirstUserProductByType(type)).isInstanceOf(UserProduct.class);
	}
}
