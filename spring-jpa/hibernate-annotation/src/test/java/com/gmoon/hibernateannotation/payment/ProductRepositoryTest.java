package com.gmoon.hibernateannotation.payment;

import static org.assertj.core.api.Assertions.assertThat;
import com.gmoon.hibernateannotation.base.BaseRepositoryTest;
import com.gmoon.hibernateannotation.payment.constants.Currency;
import com.gmoon.hibernateannotation.payment.constants.ProductType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductRepositoryTest extends BaseRepositoryTest {

	@Autowired
	ProductRepository productRepository;

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
		assertThat(productRepository.findFirstCompanyProductByType(type)).isInstanceOf(CompanyProduct.class);
		assertThat(productRepository.findFirstUserProductByType(type)).isInstanceOf(UserProduct.class);
	}
}
