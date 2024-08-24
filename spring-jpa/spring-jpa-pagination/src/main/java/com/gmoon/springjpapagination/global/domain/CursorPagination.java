package com.gmoon.springjpapagination.global.domain;

import java.io.Serializable;

public abstract class CursorPagination implements Serializable {
	protected static final Integer DEFAULT_PAGE_SIZE = 100;

	public abstract boolean isHasNextPage();

	public abstract int getPageSize();
}
