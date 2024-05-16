package com.gmoon.springjpapagination.global.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.gmoon.javacore.util.NumberUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Pageable implements Serializable {

	private static final long serialVersionUID = -2648676657463697561L;

	public static final int DEFAULT_PAGE = 1;
	public static final int DEFAULT_PAGE_SIZE = 15;
	public static final int UNPAGED_PAGE_SIZE = 0;
	public static final int DEFAULT_OFFSET = 0;

	private long totalCount;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private Integer page = 1;
	private long offset = 0;

	public Pageable initialize() {
		this.page = DEFAULT_PAGE;
		this.pageSize = UNPAGED_PAGE_SIZE;
		this.offset = DEFAULT_OFFSET;
		return this;
	}

	public Pageable initialize(long page, long pageSize, long firstRecordIndex) {
		this.page = Math.max(NumberUtils.toInt(page), DEFAULT_PAGE);
		this.pageSize = Math.max(NumberUtils.toInt(pageSize), UNPAGED_PAGE_SIZE);
		this.offset = Math.max(NumberUtils.toInt(firstRecordIndex), DEFAULT_OFFSET);
		return this;
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
