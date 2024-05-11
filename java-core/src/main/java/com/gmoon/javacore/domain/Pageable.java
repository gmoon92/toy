package com.gmoon.javacore.domain;

import java.io.Serializable;

public interface Pageable extends Serializable {

	long getTotalCount();

	int getPageSize();

	Integer getPage();

	long getOffset();

	long getTotalPages();

	long getLastPageSize();

}
