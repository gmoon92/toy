package com.gmoon.springjpapagination.global.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PaginatedContent<T> extends Pageable {

	private static final long serialVersionUID = 4042588564593515879L;

	private T content;

}
