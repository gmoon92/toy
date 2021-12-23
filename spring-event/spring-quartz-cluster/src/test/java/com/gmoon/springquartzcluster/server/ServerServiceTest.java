package com.gmoon.springquartzcluster.server;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springquartzcluster.model.WebServerSaveForm;

@Transactional
@SpringBootTest
class ServerServiceTest {
	@Autowired
	ServerService service;

	@Test
	@DisplayName("모든 활성화된 서버를 가져온다.")
	void testGetEnabledServers() {
		// given
		int savableCount = 10;
		saveDummyWebServer(savableCount);

		// when
		List<Server> servers = service.getEnabledServers();

		// then
		assertThat(servers)
			.isNotEmpty()
			.hasSize(savableCount);
	}

	@Test
	@DisplayName("서버 이름으로 서버를 가져온다.")
	void testGetServer() {
		// given
		String serverName = "gmoonKR";
		service.saveWebServer(createWebServerForm(serverName));

		// when
		Server server = service.getServer(serverName);

		// then
		assertThat(server)
			.hasFieldOrPropertyWithValue("name", serverName);
	}

	private void saveDummyWebServer(int savableCount) {
		Stream.generate(this::createWebServerForm)
			.limit(savableCount)
			.forEach(service::saveWebServer);
	}

	private WebServerSaveForm createWebServerForm() {
		return createWebServerForm("gmoon_" + UUID.randomUUID());
	}

	private WebServerSaveForm createWebServerForm(String serverName) {
		WebServerSaveForm form = new WebServerSaveForm();
		form.setName(serverName);
		form.setPublicHost("gmoon92.github.io");
		form.setPrivateHost("127.0.0.1");
		form.setPort1(443);
		form.setPort2(80);
		return form;
	}
}
