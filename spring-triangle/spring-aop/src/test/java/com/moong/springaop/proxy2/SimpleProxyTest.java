package com.moong.springaop.proxy2;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SimpleProxyTest {

	private String name;
	
	@Before
	public void init() {
		name = "Moon";
	}
	
	@Test
	@Ignore
	public void simpleProxyTest() {
		Hello proxy = new HelloTarget();
		assertThat("simple", proxy.sayHello(name), is("Hello Moon"));
		assertThat("simple", proxy.sayMyName(name), is("My name Moon"));
	}
	
	@Test // 데코레이션 기법으로 부가 기능 적용
	@Ignore
	public void simpleAspectProxyTest() {
		Hello proxy = new HelloAspect(new HelloTarget());
		assertThat("aspect", proxy.sayHello(name), is("HELLO MOON"));
		assertThat("aspect", proxy.sayMyName(name), is("MY NAME MOON"));
	}
	
	@Test // Reflection InvocationHandler를 활용한 부가 기능 적용
	public void invocationHandler() {
		/***
		 * params
		 * 1) 클래스 로더 : 다이내믹 프록시가 정의되는 클래스 로더를 지정한다.
		 * 2) 인터페이스  : 다이내믹 프록시가 구현해야 할 인터페이스
		 * 3) invocationHandler 구현체 : 부가기능 구현체
		 */
		SimpleProxy sp = new SimpleProxy( Hello.class
										, new UpperCaseHandler(new HelloTarget())
										);
		
		Hello proxy = (Hello) sp.getTarget();
		assertThat("reflect", proxy.sayHello(name), is("HELLO MOON"));
		assertThat("reflect", proxy.sayMyName(name), is("MY NAME MOON"));
	}
	
	private interface Hello{
		public String sayHello(String str);
		public String sayMyName(String str);
	}
	
	private class HelloAspect implements Hello{
		private Hello hello;
		
		public HelloAspect(Hello hello) {
			this.hello = hello;
		} 
		
		@Override
		public String sayHello(String str) {
			return hello.sayHello(str).toUpperCase(); // 위임과 부가기능 적용
		}

		@Override
		public String sayMyName(String str) {
			return hello.sayMyName(str).toUpperCase();
		}
	}
	
	//타겟 클래스
	private class HelloTarget implements Hello{
		@Override
		public String sayHello(String str) { return "Hello " + str; }

		@Override
		public String sayMyName(String str) { return "My name " + str; }
	}
}
