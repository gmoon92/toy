package com.gmoon.hibernateannotation.payments.product.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProductType {
	ENTERPRISE(Integer.MAX_VALUE),
	STANDARD(10),
	BUSINESS(5),
	FREE(Integer.MIN_VALUE);

	private final int hierarchy;

	public boolean isUpgrade(ProductType compare) {
		return hierarchy < compare.hierarchy;
	}

	public boolean isDowngrade(ProductType compare) {
		return hierarchy > compare.hierarchy;
	}
}
