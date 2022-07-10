package com.gmoon.hibernateannotation.payment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.hibernateannotation.base.BaseRepositoryTest;
import com.gmoon.hibernateannotation.payment.constants.Currency;
import com.gmoon.hibernateannotation.payment.constants.ProductType;

class ProductRepositoryTest extends BaseRepositoryTest {

	@Autowired
	ProductRepository repository;

	@Test
	void testSave() {
		// given
		ProductType productType = ProductType.ENTERPRISE;
		Product product = Product.create(productType, 10_000d, Currency.WON);

		// when then
		assertThat(repository.save(product))
			.isInstanceOf(Product.class)
			.hasFieldOrPropertyWithValue("name", productType.name())
			.hasFieldOrPropertyWithValue("type", productType);
	}
}
