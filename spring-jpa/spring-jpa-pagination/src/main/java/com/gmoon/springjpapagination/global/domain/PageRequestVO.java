package com.gmoon.springjpapagination.global.domain;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageRequestVO extends PageRequest {

	private Long offset;

	private PageRequestVO() {
		super(1, 100, Sort.unsorted());
	}

	private PageRequestVO(int page, int size) {
		this(page, size, null, Sort.unsorted());
	}

	private PageRequestVO(int page, int size, long offset) {
		this(page, size, offset, Sort.unsorted());
	}

	private PageRequestVO(int page, int size, Long offset, Sort sort) {
		super(page, size, sort);
		this.offset = offset;
	}

	public static PageRequestVO of(int page, int size, long offset) {
		return new PageRequestVO(page, size, offset);
	}

	@Override
	public long getOffset() {
		if (offset == null) {
			return super.getOffset();
		}

		return offset;
	}
}
