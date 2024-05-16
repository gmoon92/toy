package com.gmoon.springjpapagination.global.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.gmoon.javacore.util.NumberUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasePaginatedVO implements Serializable {

	public static final int DEFAULT_PAGE_SIZE = 15;

	private long totalCount;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private Integer page = 1;
	private long offset = 0;

	public BasePaginatedVO initialize() {
		this.page = 1;
		this.pageSize = 0;
		this.offset = 0;
		return this;
	}

	public BasePaginatedVO initialize(long page, long pageSize, long firstRecordIndex) {
		this.page = Math.max(NumberUtils.toInt(page), 1);
		this.pageSize = Math.max(NumberUtils.toInt(pageSize), 0);
		this.offset = Math.max(NumberUtils.toInt(firstRecordIndex), 0);
		return this;
	}


	public int getTotalPages() {
		return getTotalPage(totalCount, pageSize);
	}

	protected int getTotalPage(long totalCount, int pageSize) {
		if (pageSize == 0) {
			return 1;
		}

		return BigDecimal.valueOf(totalCount)
			.divide(BigDecimal.valueOf(pageSize), 0, RoundingMode.UP)
			.toBigInteger()
			.intValue();
	}
}
