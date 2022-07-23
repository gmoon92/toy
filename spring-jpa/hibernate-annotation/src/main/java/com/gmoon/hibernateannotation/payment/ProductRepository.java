package com.gmoon.hibernateannotation.payment;

import com.gmoon.hibernateannotation.payment.constants.ProductType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
	List<CompanyProduct> findAllByType(ProductType type);
	CompanyProduct findFirstCompanyProductByType(ProductType type);
	UserProduct findFirstUserProductByType(ProductType type);
}
