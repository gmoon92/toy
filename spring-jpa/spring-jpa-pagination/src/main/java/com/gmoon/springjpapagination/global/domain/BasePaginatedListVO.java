package com.gmoon.springjpapagination.global.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasePaginatedListVO<T> extends BasePaginatedVO {

	public static final int DEFAULT_PAGE_SIZE = 15;

	private List<T> data;

}
