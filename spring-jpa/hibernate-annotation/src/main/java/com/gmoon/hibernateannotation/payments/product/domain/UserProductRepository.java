package com.gmoon.hibernateannotation.payments.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProductRepository extends JpaRepository<UserProduct, String> {
}
