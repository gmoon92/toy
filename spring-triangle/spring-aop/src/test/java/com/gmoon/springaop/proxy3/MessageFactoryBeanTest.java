package com.gmoon.springaop.proxy3;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

// SpringBoot with Run
// @RunWith(SpringRunner.class)
// @SpringBootTest
// @Import(MessageConfig.class)
// @ContextConfiguration(classes = {MessageConfig.class})
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {MessageConfig.class})
class MessageFactoryBeanTest {

	@Autowired
	private ApplicationContext context;

	@Test
	void getMessageFromFactoryBean() {
		// given
		String beanName = "message";

		// when
		Object message = context.getBean(beanName);

		// then
		assertThat(message)
			 .isInstanceOf(Message.class)
			 .hasFieldOrPropertyWithValue("text", "Factory Bean"); // factory bean이 생성해주는 object
	}

	/**
	 * 팩토리 빈이 만들어주는 빈 오브젝트가 아닌 픽토리 빈 자체를 가져오고 싶을 경우엔
	 * getBean 메소드에 '&'를 빈 이름 앞에 붙여주면 팩토리 빈 자체를 반환한다.
	 * &가 붙고 안 붙고에 따라 getBean() 메소드가 돌려주는 오브젝트가 달라진다.
	 * */
	@Test
	void getFactoryBean() {
		// given
		String factoryBeanName = "&message";

		// when
		Object factory = context.getBean(factoryBeanName);

		// then
		assertThat(factory)
			 .isInstanceOf(MessageFactoryBean.class); // <- bean
	}
}
