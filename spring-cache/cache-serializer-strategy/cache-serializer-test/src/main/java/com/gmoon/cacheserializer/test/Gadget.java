package com.gmoon.cacheserializer.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

public class Gadget implements Serializable {

	public static final AtomicBoolean EXECUTED = new AtomicBoolean();

	@Serial
	private static final long serialVersionUID = 1L;

	@Serial
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		EXECUTED.set(true);
	}
}
