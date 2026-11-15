package com.gmoon.cacheserializer.test;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Parent implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private final List<Child> children = new ArrayList<>();

	public static Parent withChild() {
		Parent parent = new Parent();
		parent.children.add(new Child(parent));
		return parent;
	}

	public List<Child> getChildren() {
		return children;
	}

	public static class Child implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		private final Parent parent;

		Child(Parent parent) {
			this.parent = parent;
		}

		public Parent getParent() {
			return parent;
		}
	}
}
