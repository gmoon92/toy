package com.gmoon.javacore.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.gmoon.javacore.util.NumberUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasePaginatedVO implements Pageable {

	public static final int DEFAULT_PAGE_SIZE = 15;

	private long totalCount;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private Integer page = 1;
	private long offset = 0;

	public ZeroBasedPage getZeroBasedPage() {
		return ZeroBasedPage.adjust(getPage());
	}

	@Override
	public Integer getPage() {
		return NumberUtils.defaultInteger(page, 1);
	}

	@Override
	public long getTotalPages() {
		return obtainTotalPage(getTotalCount());
	}

	@Override
	public long getLastPageSize() {
		return obtainLastPageSize(getTotalCount());
	}

	protected long obtainTotalPage(long totalCount) {
		int pageSize = getPageSize();
		return BigDecimal.valueOf(totalCount)
			.divide(BigDecimal.valueOf(pageSize), 0, RoundingMode.UP)
			.toBigInteger()
			.intValue();
	}

	protected long obtainLastPageSize(long totalCount) {
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
