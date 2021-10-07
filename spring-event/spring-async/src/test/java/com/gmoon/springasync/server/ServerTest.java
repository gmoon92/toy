package com.gmoon.springasync.server;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServerTest {
	@Test
	@DisplayName("포트를 포함한 웹 서버 url 을 반환한다.")
	void getWebServerUrl() {
		// given
		Server server = Server.createGithubBlogServer();

		// when
		String url = server.getWebServerUrl(true);

		// then
		assertThat(url)
			.isEqualTo("https://gmoon92.github.io:443");
	}
}