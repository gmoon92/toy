package com.gmoon.springasync;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("application.yml 메일 설정 후 테스트 진행 "
	 + "username: @test.mail.username@"
	 + "password: @test.mail.password@")
class SpringAsyncApplicationTests {

	@Test
	void contextLoads() {
	}

}
