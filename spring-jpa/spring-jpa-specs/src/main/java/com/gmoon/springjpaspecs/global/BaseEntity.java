package com.gmoon.springjpaspecs.global;

import java.io.Serializable;

public abstract class BaseEntity<ID extends Serializable> implements Serializable {

	public abstract ID getId();
}
