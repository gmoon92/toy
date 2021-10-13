package com.gmoon.springasync.server;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
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