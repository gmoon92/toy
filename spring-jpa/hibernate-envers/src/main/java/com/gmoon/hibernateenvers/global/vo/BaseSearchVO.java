package com.gmoon.hibernateenvers.global.vo;

import java.util.Date;

import org.springframework.data.domain.Sort;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public abstract class BaseSearchVO extends BasePageable {

	public BaseSearchVO(Sort sort) {
		super(sort);
	}

	private Date startDt;

	private Date endDt;

	private String searchKeyword;

}
