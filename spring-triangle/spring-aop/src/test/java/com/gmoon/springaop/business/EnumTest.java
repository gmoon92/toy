package com.gmoon.springaop.business;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EnumTest {
	private UserVO user1;
	private UserVO user2;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@BeforeAll
	void init() {
		user1 = new UserVO(1, "Moon", Level.BASIC);
		user2 = new UserVO(1, "Moon", Level.SILVER);
	}

	@Test
	void enumTest() {
		int userLevel1 = user1.getLevel().getValue();
		int userLevel2 = user2.getLevel().getValue();
		assertThat(userLevel1).isEqualTo(1);
		assertThat(userLevel2).isEqualTo(2);
	}

	@Test
	void loggerTest() {
		logger.info("\n targetInfo : \n Class Name : {} Method Name : {} \n Params : {}", "test1", "test2", "test3");
	}

}
