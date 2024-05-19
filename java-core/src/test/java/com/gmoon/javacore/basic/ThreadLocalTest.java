package com.gmoon.javacore.basic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class ThreadLocalTest {

	@Test
	void learning() {
		ThreadLocal<List<String>> threadLocal = ThreadLocal.withInitial(ArrayList::new);

		List<String> filedNames = Arrays.stream(Account.class.getDeclaredFields())
			 .map(Field::getName)
			 .collect(Collectors.toList());

		threadLocal.set(filedNames);
		System.out.println(Thread.currentThread().getName() + ", objs: " + threadLocal.get());
	}

	static class Account {
		private String username;
	}
}
