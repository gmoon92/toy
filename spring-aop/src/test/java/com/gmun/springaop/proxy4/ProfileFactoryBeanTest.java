package com.gmun.springaop.proxy4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gmun.springaop.business.UserService;
import com.gmun.springaop.business.UserServiceImple;
import com.gmun.springaop.business.UserVO;

/*
//SpringBoot with Run
@RunWith(SpringRunner.class)
@SpringBootTest
@Import(MessageConfig.class)
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UserServiceConfig.class})
public class ProfileFactoryBeanTest {

	@Autowired
	private ApplicationContext context;
	
	@Test
	@DirtiesContext
	public void profileFactoryBeanTest() throws Exception {
		UserService userService = new UserServiceImple(new UserVO(0, "Moon", null));
		ProfileFactoryBean proxy = (ProfileFactoryBean)context.getBean("&pxuserService", ProfileFactoryBean.class);
		proxy.setPattern("updateLevels");
		proxy.setProfile(new ProfileImple());
		proxy.setTarget(userService);
		
		proxy.setServiceInterface(UserService.class);
		
		UserService user = (UserService)proxy.getObject();
					user.updateLevels();
		
	}
}