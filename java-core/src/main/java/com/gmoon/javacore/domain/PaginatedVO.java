package com.gmoon.javacore.domain;

import static com.gmoon.javacore.util.NumberUtils.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginatedVO implements BasePageable {

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

		this.page = toInt(zeroBasedPage) + 1;
		this.pageSize = toInt(adjustedPageSize);
		this.offset = adjustedOffset;
	}
}
