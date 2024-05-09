package com.gmoon.javacore.test.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class PaginatedVO {

	public static final int DEFAULT_PAGE_SIZE = 15;

	private long totalCount;
	private int pageSize;
	private Integer page;
	private Integer offset;

	public PaginatedVO() {
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.page = 1;
		this.offset = 0;
	}

	public void resizingPage(long prevTotalCount) {
		final long requestPage = getPage();
		final long pageSize = getPageSize();

		final int dataPresent = 1;
		final long resizingStartPage = obtainTotalPage(prevTotalCount + dataPresent);
		final long prevLastPageSize = getLastPageSize(prevTotalCount);

		long adjustedPage = Math.max(requestPage - resizingStartPage, 0);
		long adjustedPageSize = 0;
		long adjustedOffset = adjustedPage * pageSize;

		if (requestPage == resizingStartPage) {
			adjustedPageSize = pageSize - prevLastPageSize;
		} else if (requestPage > resizingStartPage) {
			adjustedPageSize = pageSize;
			adjustedOffset -= prevLastPageSize;
		}

		this.page = Math.toIntExact(adjustedPage);
		this.pageSize = Math.toIntExact(adjustedPageSize);
		this.offset = Math.toIntExact(adjustedOffset);
	}

	private long getLastPageSize(long totalCount) {
		int pageSize = getPageSize();
		int totalPage = obtainTotalPage(totalCount);
		long lastPageSize = pageSize - ((long)totalPage * pageSize - totalCount);
		if (pageSize == lastPageSize) {
			return 0;
		}
		return lastPageSize;
	}

	private int obtainTotalPage(long prevTotalCount) {
		long pageSize = getPageSize();
		return new BigDecimal(prevTotalCount)
			.divide(new BigDecimal(pageSize), 0, RoundingMode.UP)
			.toBigInteger()
			.intValue();
	}
}
