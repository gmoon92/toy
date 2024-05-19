package com.gmoon.hibernateannotation.payments.product.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.hibernateannotation.payments.product.domain.vo.ProductType;

public interface ProductRepository extends JpaRepository<Product, String> {
	List<CompanyProduct> findAllByType(ProductType type);

	CompanyProduct findFirstCompanyProductByType(ProductType type);

	UserProduct findFirstUserProductByType(ProductType type);
}
