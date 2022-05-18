package com.gmoon.javacore.basic;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;

public class CastingTest {
	@Test
	void testDownCasting() {
		Parent parent = new Child("gmoon");

		Child child = (Child)parent;

		assertThat(child.name).isEqualTo("gmoon");
	}

	abstract static class Parent { }

	@RequiredArgsConstructor
	static class Child extends Parent {
		private final String name;
	}
}
