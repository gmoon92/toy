package com.gmoon.hibernateenvers.global.model;

import org.springframework.data.domain.Sort;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public abstract class BaseSearchVO extends BasePageable {

	private Long startTime;

	private String searchKeyword;
	private Long endTime;
	protected BaseSearchVO(Sort sort) {
		super(sort);
	}

}
