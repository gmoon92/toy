package com.moong.springaop.proxy1;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;


public class ReflectionTest {

	private Reflection rf;
	
	@Before
	public void init() {
		rf = new Reflection();
	}
	
	@Test
	public void invokTest() throws Exception {
		String name = "Moon";
		
		assertThat("length test", name.length(), is(4));
		assertThat("length test", name.length(), is(rf.stringLength(name)));
		
		assertThat("charAt test", name.charAt(0), is('M'));
		assertThat("charAt test", name.charAt(0), is(rf.stringCharAt(name, 0)));
	}
	
	/**
	 * 다이나믹 프로젝트는 이런식으로 Proxy Object가 생성되지 않는다.
	 * */
	@Test
	public void dateObject() throws Exception {
		String str = (String)rf.createObject("java.lang.String");
			   str = "test";
			   
		assertThat(str.length(), is(4));
	}
}
