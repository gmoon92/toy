package com.gmoon.springasync.mail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("application.yml 메일 설정 후 테스트 진행 "
	+ "username: @test.mail.username@"
	+ "password: @test.mail.password@")
class MailControllerTest {

	@Autowired
	TestRestTemplate restTemplate;

	@LocalServerPort
	int port;

	@Test
	@DisplayName("비동기 메일 발송 처리")
	void asyncSendMail() {
		restTemplate.getForEntity(String.format("http://localhost:%d/mail/send/invite", port), Void.class);
	}
}
