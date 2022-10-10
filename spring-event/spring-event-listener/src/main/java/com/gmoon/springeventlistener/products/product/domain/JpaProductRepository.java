package com.gmoon.springeventlistener.products.product.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductRepository extends JpaRepository<Product, String> {

	Optional<Product> findProductByName(String productName);
}
