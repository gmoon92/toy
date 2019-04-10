package com.gmun.springaop.proxy3;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
//SpringBoot with Run
@RunWith(SpringRunner.class)
@SpringBootTest
@Import(MessageConfig.class)
@ContextConfiguration(classes = {MessageConfig.class})
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MessageConfig.class})
public class MessageFactoryBeanTest {

	@Autowired
	private ApplicationContext context;
	
	@Test
	public void getMessageFromFactoryBean(){
		Object message = context.getBean("message");
		assertThat(message, instanceOf(Message.class));
		assertThat( ( (Message) message ).getText(), is("Factory Bean")); // <- factory bean이 생성해주는 object
	}
	
	@Test
	public void getFactoryBean() {
		/**
		 * 팩토리 빈이 만들어주는 빈 오브젝트가 아닌 픽토리 빈 자체를 가져오고 싶을 경우엔
		 * getBean 메소드에 '&'를 빈 이름 앞에 붙여주면 팩토리 빈 자체를 반환한다.
		 * */
		Object factory = context.getBean("&message"); // &가 붙고 안 붙고에 따라 getBean() 메소드가 돌려주는 오브젝트가 달라진다.
		assertThat(factory, instanceOf(MessageFactoryBean.class)); // <- bean
	}
}