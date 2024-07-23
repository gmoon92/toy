package com.gmoon.javacore.bytebuddy;

import com.gmoon.javacore.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ByteBuddyTest {

	@Test
	void intercept() {
		ClassLoader classLoader = getClass().getClassLoader();
		DynamicType.Unloaded<Foo> dynamicType = new ByteBuddy()
//			 .redefine(Poo.class)
			 .subclass(Foo.class)
			 .method(ElementMatchers.named("getString"))
			 .intercept(FixedValue.value("Hello World!"))
			 .make();

		Class<? extends Foo> clazz = dynamicType
			 .load(classLoader, ClassLoadingStrategy.Default.WRAPPER)
//			 .load(classLoader, ClassLoadingStrategy.Default.WRAPPER_PERSISTENT)
//			 .load(classLoader, ClassLoadingStrategy.Default.CHILD_FIRST)
//			 .load(classLoader, ClassLoadingStrategy.Default.CHILD_FIRST_PERSISTENT)
			 .getLoaded();

		Foo foo = ReflectionUtils.newInstance(clazz);
		String actual = foo.getString("hi");
		assertThat(actual).isEqualTo("Hello World!");
	}

	@Test
	void proxy() {
		ClassLoader classLoader = getClass().getClassLoader();
		DynamicType.Unloaded<Foo> dynamicType = new ByteBuddy()
			 .subclass(Foo.class)
			 .method(
				  ElementMatchers.named("getString")
					   .and(ElementMatchers.isDeclaredBy(Foo.class))
					   .and(ElementMatchers.returns(String.class))
			 )
			 .intercept(MethodDelegation.to(FooProxy.class))
			 .make();

		Class<? extends Foo> clazz = dynamicType
//			 .load(classLoader)
			 .load(classLoader, ClassLoadingStrategy.Default.WRAPPER)
			 .getLoaded();

		Foo foo = ReflectionUtils.newInstance(clazz);
		String actual = foo.getString("hi");
		assertThat(actual).isEqualTo("proxy_hi");
	}

	public static class Foo {

		public String getString(String str) {
			log.info(str);
			return str;
		}
	}

	public static class FooProxy {

		public static String intercept(String str) {
			return "proxy_" + str;
		}
	}
}
