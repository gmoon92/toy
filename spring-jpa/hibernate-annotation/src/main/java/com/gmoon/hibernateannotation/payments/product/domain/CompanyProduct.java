package com.gmoon.hibernateannotation.payments.product.domain;

import com.gmoon.hibernateannotation.payments.price.domain.Currency;
import com.gmoon.hibernateannotation.payments.product.domain.vo.ProductType;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("null")
@PrimaryKeyJoinColumn(name = "company_product_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CompanyProduct extends Product {

	public CompanyProduct(ProductType productType) {
		super(productType);
	}

	public static CompanyProduct create(ProductType productType, Double price, Currency currency) {
		return (CompanyProduct)new CompanyProduct(productType)
			 .withPrice(price, currency);
	}
}
