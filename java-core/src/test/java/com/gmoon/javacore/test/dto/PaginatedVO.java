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
	private long fullListSize;
	private String sortTargetColumn;
	private Integer page;
	private Integer firstRecordIndex;
	private Integer offset;

	public PaginatedVO() {
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.page = 1;
		this.firstRecordIndex = 0;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		this.firstRecordIndex = (this.page - 1) * this.pageSize;
	}

	public void setPage(Integer page) {
		if (page == null || page.compareTo(1) < 0)
			page = 1;
		this.page = page;
		this.firstRecordIndex = obtainOffset(this.page, this.pageSize);
	}

	protected int obtainOffset(int page, int pageSize) {
		return (page - 1) * pageSize;
	}

	public void resizingPage(long pageSize, long firstRecordIndex) {
		this.pageSize = Math.toIntExact(pageSize);
		this.firstRecordIndex = Math.toIntExact(firstRecordIndex);
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

		resizingPage(adjustedPageSize, adjustedOffset);
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
			.divide(new BigDecimal(pageSize), RoundingMode.UP)
			.setScale(0, RoundingMode.DOWN)
			.toBigInteger()
			.intValue();
	}
}
