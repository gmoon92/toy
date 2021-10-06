package com.gmoon.springaop.proxy2;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;

class SimpleProxyTest {
	private String name;

	@BeforeAll
	void init() {
		name = "Moon";
	}

	@Test
	@Disabled
	void simpleProxyTest() {
		Hello proxy = new HelloTarget();
		assertThat(proxy.sayHello(name)).isEqualTo("Hello Moon");
		assertThat(proxy.sayMyName(name)).isEqualTo("My name Moon");
	}

	@Test
	@DisplayName("데코레이션 기법으로 부가 기능 적용")
	@Disabled
	void simpleAspectProxyTest() {
		Hello proxy = new HelloAspect(new HelloTarget());
		assertThat(proxy.sayHello(name)).isEqualTo("HELLO MOON");
		assertThat(proxy.sayMyName(name)).isEqualTo("MY NAME MOON");
	}

	@Test
	@DisplayName("Reflection InvocationHandler를 활용한 부가 기능 적용")
	void invocationHandler() {
		/***
		 * params
		 * 1) 클래스 로더 : 다이내믹 프록시가 정의되는 클래스 로더를 지정한다.
		 * 2) 인터페이스  : 다이내믹 프록시가 구현해야 할 인터페이스
		 * 3) invocationHandler 구현체 : 부가기능 구현체
		 */
		SimpleProxy simpleProxy = new SimpleProxy(Hello.class, new UpperCaseHandler(new HelloTarget()));

		Hello proxy = (Hello)simpleProxy.getTarget();
		assertThat(proxy.sayHello(name)).isEqualTo("HELLO MOON");
		assertThat(proxy.sayMyName(name)).isEqualTo("MY NAME MOON");
	}

	@RequiredArgsConstructor
	class HelloAspect implements Hello {
		private final Hello hello;

		// 위임과 부가기능 적용
		@Override
		public String sayHello(String str) {
			return hello.sayHello(str).toUpperCase();
		}

		@Override
		public String sayMyName(String str) {
			return hello.sayMyName(str).toUpperCase();
		}
	}

	//타겟 클래스
	class HelloTarget implements Hello {
		@Override
		public String sayHello(String str) {
			return "Hello " + str;
		}

		@Override
		public String sayMyName(String str) {
			return "My name " + str;
		}
	}

	interface Hello {
		String sayHello(String str);
		String sayMyName(String str);
	}

}
