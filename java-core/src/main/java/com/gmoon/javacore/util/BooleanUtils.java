package com.gmoon.javacore.util;

public class BooleanUtils {
	public static boolean toBoolean(Boolean bool) {
		return org.apache.commons.lang3.BooleanUtils.toBoolean(bool);
	}

	public static boolean toBoolean(Integer integer) {
		return org.apache.commons.lang3.BooleanUtils.toBoolean(integer);
	}
}
