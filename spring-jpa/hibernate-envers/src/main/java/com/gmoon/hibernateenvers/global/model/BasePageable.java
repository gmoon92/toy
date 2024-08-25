package com.gmoon.hibernateenvers.global.model;

import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public abstract class BasePageable {

	private Integer block = 5;
	protected Integer page = 1;
	private Sort sort;

	protected BasePageable(Sort sort) {
		this.sort = Objects.requireNonNullElse(sort, getDefaultSort());
	}

	public Pageable getPageable() {
		return PageRequest.of(this.page - 1 // view에서 페이지 번호로 직접 넘겨줌, jpa 시작은 0 부터
			 , this.block // page block limit
			 , this.sort
		);
	}

	private Sort getDefaultSort() {
		return Sort.by(Sort.Direction.DESC, "id");
	}
}
