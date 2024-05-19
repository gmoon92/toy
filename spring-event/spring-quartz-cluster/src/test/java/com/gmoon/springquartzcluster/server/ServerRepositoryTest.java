package com.gmoon.springquartzcluster.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gmoon.springquartzcluster.model.WebServerSaveForm;
import com.gmoon.springquartzcluster.test.BaseDataJpaTest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ServerRepositoryTest extends BaseDataJpaTest {
	final ServerRepository repository;

	@Test
	void testCount() {
		assertThat(repository.count())
			 .isInstanceOf(Long.class);
	}

	@DisplayName("활성화된 웹 서버를 생성 조회")
	@Test
	void testSaveAndGet() {
		// given
		String serverName = "gmoon-web";
		WebServerSaveForm form = new WebServerSaveForm();
		form.setName(serverName);
		form.setPublicHost("gmoon92.github.io");
		form.setPrivateHost("127.0.0.1");
		form.setPort1(443);
		form.setPort2(80);

		// when
		Server webServer = repository.save(form.createEnabledWebServer());

		// then
		assertThat(webServer)
			 .isEqualTo(repository.findServerByName(serverName));
	}

	@DisplayName("활성화된 서버 조회")
	@Test
	void testGetEnabledServers() {
		// when then
		assertThatCode(() -> repository.getEnabledServers())
			 .doesNotThrowAnyException();
	}
}
