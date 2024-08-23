package com.gmoon.springaop.proxy2;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

class SimpleProxyTest {

	@Test
	void simpleProxyTest() {
		Hello proxy = new HelloTarget();
		assertThat(proxy.sayHello("Moon")).isEqualTo("Hello Moon");
		assertThat(proxy.sayMyName("Moon")).isEqualTo("My name Moon");
	}

	@Test
	@DisplayName("데코레이션 기법으로 부가 기능 적용")
	void simpleAspectProxyTest() {
		Hello proxy = new HelloAspect(new HelloTarget());
		assertThat(proxy.sayHello("Moon")).isEqualTo("HELLO MOON");
		assertThat(proxy.sayMyName("Moon")).isEqualTo("MY NAME MOON");
	}

	@DisplayName("Reflection InvocationHandler를 활용한 부가 기능 적용")
	@Test
	void invocationHandler() {
		/***
		 * params
		 * 1) 클래스 로더 : 다이내믹 프록시가 정의되는 클래스 로더를 지정한다.
		 * 2) 인터페이스  : 다이내믹 프록시가 구현해야 할 인터페이스
		 * 3) invocationHandler 구현체 : 부가기능 구현체
		 */
		SimpleProxy simpleProxy = new SimpleProxy(Hello.class, new UpperCaseHandler(new HelloTarget()));
		Hello proxy = (Hello)simpleProxy.getTarget();

		assertThat(proxy.sayHello("Moon")).isEqualTo("HELLO MOON");
		assertThat(proxy.sayMyName("Moon")).isEqualTo("MY NAME MOON");
		assertThatCode(() -> proxy.setValue("Moon")).doesNotThrowAnyException();
		assertThatCode(proxy::getValue).doesNotThrowAnyException();

	}

	@DisplayName("Proxy 자기 호출 부가 기능 불가")
	@Test
	void selfInvocation() {
		SimpleProxy simpleProxy = new SimpleProxy(Hello.class, new UpperCaseHandler(new HelloTarget()));
		Hello proxy = (Hello)simpleProxy.getTarget();

		// self invoke
		Hello self = proxy.channing();
		assertThat(self.sayHello("Moon")).isEqualTo("Hello Moon");
		assertThat(self.sayMyName("Moon")).isEqualTo("My name Moon");
	}

	interface Hello {
		String sayHello(String str);

		String sayMyName(String str);

		String getValue();

		void setValue(String str);

		Hello channing();
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

		@Override
		public String getValue() {
			return hello.getValue();
		}

		@Override
		public void setValue(String str) {
			hello.setValue(str);
		}

		@Override
		public Hello channing() {
			return hello.channing();
		}
	}

	//타겟 클래스
	@Getter
	@Setter
	class HelloTarget implements Hello {

		private String value;

		@Override
		public String sayHello(String str) {
			return "Hello " + str;
		}

		@Override
		public String sayMyName(String str) {
			return "My name " + str;
		}

		@Override
		public Hello channing() {
			return this;
		}
	}

}
