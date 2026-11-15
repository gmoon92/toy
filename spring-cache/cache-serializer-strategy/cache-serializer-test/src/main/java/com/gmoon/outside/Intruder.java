package com.gmoon.outside;

import java.util.concurrent.atomic.AtomicBoolean;

public class Intruder {

	public static final AtomicBoolean CONSTRUCTED = new AtomicBoolean();

	public Intruder() {
		CONSTRUCTED.set(true);
	}
}
