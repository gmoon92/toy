package com.gmoon.springjpapagination.common;

import java.io.Serializable;

public abstract class CursorPagination implements Serializable {
	protected static final Integer DEFAULT_PAGE_SIZE = 5;
	public static final String EMPTY_CURSOR = "-1";

	public abstract String getCursor();
	public abstract boolean isHasNextPage();

	public abstract int getPageSize();
}
