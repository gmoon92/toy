package com.gmoon.javacore.test.domain;

import com.gmoon.javacore.persistence.Id;
import java.util.UUID;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseUser {

	@Id
	@EqualsAndHashCode.Include
	protected UUID id;
}
