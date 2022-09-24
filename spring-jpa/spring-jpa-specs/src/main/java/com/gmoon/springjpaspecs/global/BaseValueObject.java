package com.gmoon.springjpaspecs.global;

import java.io.Serializable;

public abstract class BaseValueObject implements Serializable {

	public abstract boolean equals(Object o);
	public abstract int hashCode();
}
