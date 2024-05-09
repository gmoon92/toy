package com.gmoon.javacore.domain;

import static com.gmoon.javacore.util.NumberUtils.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginatedVO {

	public static final int DEFAULT_PAGE_SIZE = 15;

	private long totalCount;
	private int pageSize;
	private Integer page;
	private long offset;

	public PaginatedVO() {
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.page = 1;
		this.offset = 0;
	}

	// todo zerobased.
	public void resizingPage(long prevTotalCount) {
		final long requestPage = getPage();
		final long pageSize = getPageSize();

		final int dataPresent = 1;
		final long resizingStartPage = obtainTotalPage(prevTotalCount + dataPresent);
		final long prevLastPageSize = getLastPageSize(prevTotalCount);

		long zeroBasedPage = positiveNumberOrZero(requestPage - resizingStartPage);
		long adjustedPageSize = 0;
		long adjustedOffset = zeroBasedPage * pageSize;

		if (requestPage == resizingStartPage) {
			adjustedPageSize = pageSize - prevLastPageSize;
		} else if (requestPage > resizingStartPage) {
			adjustedPageSize = pageSize;
			adjustedOffset -= prevLastPageSize;
		}

		this.page = toInt(zeroBasedPage);
		this.pageSize = toInt(adjustedPageSize);
		this.offset = adjustedOffset;
	}

	private long getLastPageSize(long totalCount) {
		int pageSize = getPageSize();
		long totalPage = obtainTotalPage(totalCount);
		long totalPageSize = totalPage * pageSize;
		long lastPageSize = pageSize - (totalPageSize - totalCount);
		if (pageSize == lastPageSize) {
			return 0;
		}
		return lastPageSize;
	}

	private long obtainTotalPage(long prevTotalCount) {
		long pageSize = getPageSize();
		return new BigDecimal(prevTotalCount)
			.divide(new BigDecimal(pageSize), 0, RoundingMode.UP)
			.toBigInteger()
			.intValue();
	}
}
