package com.gmoon.javacore.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public interface BasePageable extends Serializable {

	long getTotalCount();

	int getPageSize();

	Integer getPage();

	long getOffset();

	default ZeroBasedPage getZeroBasedPage() {
		return ZeroBasedPage.adjust(getPage());
	}

	default long obtainTotalPage(long prevTotalCount) {
		int pageSize = getPageSize();
		return BigDecimal.valueOf(prevTotalCount)
			.divide(BigDecimal.valueOf(pageSize), 0, RoundingMode.UP)
			.toBigInteger()
			.intValue();
	}

	default long getLastPageSize(long totalCount) {
		int pageSize = getPageSize();
		long totalPage = obtainTotalPage(totalCount);
		long totalPageSize = totalPage * pageSize;
		long lastPageSize = pageSize - (totalPageSize - totalCount);
		if (pageSize == lastPageSize) {
			return 0;
		}
		return lastPageSize;
	}
}
