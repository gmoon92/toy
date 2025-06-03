package com.gmoon.querydslsql.querydslsqlmaven.logging;

public enum Color {
	RESET("\u001B[0m"),
	RED("\u001B[31m"),
	GREEN("\u001B[32m"),
	YELLOW("\u001B[33m"),
	BLUE("\u001B[34m"),
	CYAN("\u001B[36m");

	private final String code;

	Color(String code) {
		this.code = code;
	}

	public String apply(String text) {
		return code + text + RESET.code;
	}

	public static String bold(Color color, String text) {
		return "\u001B[1m" + color.code + text + RESET.code;
	}
}
