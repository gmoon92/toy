package com.gmoon.springjpaspecs.global.domain;

import java.io.Serializable;

public abstract class EntityObject<ID extends Serializable> implements Serializable {

	public abstract ID getId();
}
