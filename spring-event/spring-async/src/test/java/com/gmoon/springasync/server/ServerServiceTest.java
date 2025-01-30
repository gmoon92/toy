package com.gmoon.springasync.server;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("application.yml 메일 설정 후 테스트 진행 "
	 + "username: @test.mail.username@"
	 + "password: @test.mail.password@")
class ServerServiceTest {
	@Autowired
	ServerService serverService;

	@Test
	@DisplayName("요청에 따라 프로토콜(http/https)을 맞게 반환된다.")
	void getWebServerWithoutPortUrl() {
		// given
		Server blogServer = Server.createGithubBlogServer();

		// when
		String url = serverService.getWebServerWithoutPortUrl(blogServer);

		// then
		assertThat(url)
			 .isEqualTo("http://gmoon92.github.io");
	}
}
