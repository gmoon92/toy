package com.moong.springaop.proxy4;

import com.moong.springaop.business.UserService;
import com.moong.springaop.business.UserServiceImpl;
import com.moong.springaop.business.UserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {UserServiceConfig.class})
class ProfileFactoryBeanTest {

	@Autowired
	private ApplicationContext context;
	
	@Test
	@DirtiesContext
	void profileFactoryBeanTest() throws Exception {
		UserService userService = new UserServiceImpl(new UserVO(0, "Moon", null));
		ProfileFactoryBean proxy = (ProfileFactoryBean)context.getBean("&pxuserService", ProfileFactoryBean.class);
		proxy.setPattern("updateLevels");
		proxy.setProfile(new ProfileImple());
		proxy.setTarget(userService);
		
		proxy.setServiceInterface(UserService.class);
		
		UserService user = (UserService)proxy.getObject();
					user.updateLevels();
	}
}