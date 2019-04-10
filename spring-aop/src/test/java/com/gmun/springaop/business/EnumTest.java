package com.gmun.springaop.business;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumTest {

	private UserVO user1;
	private UserVO user2;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Before
	public void init() {
		user1 = new UserVO(1, "Moon", Level.BASIC);
		user2 = new UserVO(1, "Moon", Level.SILVER);
	}
	
	@Test
	public void enumTest() {
		int userLevel1 = user1.getLevel().getValue();
		int userLevel2 = user2.getLevel().getValue();
		
		assertThat(userLevel1, is(1));
		assertThat(userLevel2, is(2));
	}
	
	@Test
	public void loggerTest() {
		logger.info("\n targetInfo : \n Class Name : {} Method Name : {} \n Params : {}", "test1", "test2", "test3");
	}
	
}
