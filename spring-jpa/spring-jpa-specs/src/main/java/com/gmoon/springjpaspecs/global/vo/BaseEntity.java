package com.gmoon.springjpaspecs.global.vo;

import java.io.Serializable;

public abstract class BaseEntity<ID extends Serializable> implements Serializable {

	public abstract ID getId();
}
