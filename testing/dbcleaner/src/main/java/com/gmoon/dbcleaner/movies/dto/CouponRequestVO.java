package com.gmoon.dbcleaner.movies.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CouponRequestVO implements Serializable {

	private Long officeId;
	private Long movieId;
}
