package com.gmoon.springjpapagination.global.domain;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasePaginatedVO implements Serializable, Pageable {

	public static final int DEFAULT_PAGE_SIZE = 15;

	private long totalCount;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private Integer page = 1;
	private long offset = 0;

	private Pageable pageable() {
		return PageRequestVO.of(page, pageSize, offset);
	}

	@Override
	public Optional<Pageable> toOptional() {
		return pageable().toOptional();
	}

	@Override
	public Sort getSortOr(Sort sort) {
		return pageable().getSortOr(sort);
	}

	@Override
	public boolean isUnpaged() {
		return pageable().isUnpaged();
	}

	@Override
	public boolean isPaged() {
		return pageable().isPaged();
	}

	@Override
	public boolean hasPrevious() {
		return pageable().hasPrevious();
	}

	@Override
	public Pageable withPage(int pageNumber) {
		return pageable().withPage(pageNumber);
	}

	@Override
	public Pageable first() {
		return pageable().first();
	}

	@Override
	public Pageable previousOrFirst() {
		return pageable().previousOrFirst();
	}

	@Override
	public Pageable next() {
		return pageable().next();
	}

	@Override
	public Sort getSort() {
		return pageable().getSort();
	}

	@Override
	public int getPageNumber() {
		return pageable().getPageNumber();
	}
}
