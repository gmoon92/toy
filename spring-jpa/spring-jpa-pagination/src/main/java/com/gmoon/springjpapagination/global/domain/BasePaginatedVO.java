package com.gmoon.springjpapagination.global.domain;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasePaginatedVO<T> implements Serializable {

	public static final int DEFAULT_PAGE_SIZE = 15;

	private List<T> data;
	private long totalCount;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private Integer page = 1;
	private long offset = 0;

}
