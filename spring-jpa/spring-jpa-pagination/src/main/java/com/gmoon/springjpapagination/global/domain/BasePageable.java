package com.gmoon.springjpapagination.global.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.gmoon.javacore.util.NumberUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasePageable implements Serializable {

	private static final long serialVersionUID = -2648676657463697561L;

	private static final BasePageable UNPAGED = new UnpagedPageable();

	public static final int DEFAULT_PAGE = 1;
	public static final int DEFAULT_PAGE_SIZE = 15;
	public static final int UNPAGED_PAGE_SIZE = 0;
	public static final int DEFAULT_OFFSET = 0;

	private long totalCount;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private Integer page = 1;
	private long offset = 0;

	public BasePageable initialize() {
		this.page = DEFAULT_PAGE;
		this.pageSize = UNPAGED_PAGE_SIZE;
		this.offset = DEFAULT_OFFSET;
		return this;
	}

	public BasePageable initialize(long page, long pageSize, long firstRecordIndex) {
		this.page = Math.max(NumberUtils.toInt(page), DEFAULT_PAGE);
		this.pageSize = Math.max(NumberUtils.toInt(pageSize), UNPAGED_PAGE_SIZE);
		this.offset = Math.max(NumberUtils.toInt(firstRecordIndex), DEFAULT_OFFSET);
		return this;
	}

	public static BasePageable unpaged() {
		return UNPAGED;
	}

	private static class UnpagedPageable extends BasePageable {
	}

	public int getTotalPages() {
		return getTotalPage(totalCount, pageSize);
	}

	protected int getTotalPage(long totalCount, int pageSize) {
		if (pageSize == UNPAGED_PAGE_SIZE) {
			return 1;
		}

		return BigDecimal.valueOf(totalCount)
			 .divide(BigDecimal.valueOf(pageSize), 0, RoundingMode.UP)
			 .toBigInteger()
			 .intValue();
	}
}
