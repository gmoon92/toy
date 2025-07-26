package com.gmoon.springpoi.base.persistence.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnLength {

	public static final int ID = 50;
	public static final int USERNAME = 20;

	public static final int SHA_256 = 64;
	public static final int ENUM = 20;
	public static final int PASSWORD_BCRYPT = 60;
}
