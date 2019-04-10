package com.gmun.springaop.business;

public enum Level {

	BASIC(1), SILVER(2), GOLD(3);
	
	private final int value;
	
	Level(int value){
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static Level valueOf(int value) {
		switch (value) {
			case 1: return Level.BASIC;
			case 2: return Level.SILVER;
			case 3: return Level.GOLD;
			default: throw new AssertionError("Unknown value: " + value);
		}
	}
	
}
