package com.gmoon.springquartzcluster.server;

import static org.assertj.core.api.Assertions.assertThat;
import com.gmoon.springquartzcluster.model.WebServerSaveForm;
import com.gmoon.springquartzcluster.test.BaseDataJpaTest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor
class ServerRepositoryTest extends BaseDataJpaTest {
	final ServerRepository repository;

	@Test
	void testCount() {
		assertThat(repository.count())
			.isInstanceOf(Long.class);
	}

	@Order(Integer.MIN_VALUE)
	@Test
	@DisplayName("활성화된 웹 서버를 생성한다")
	void testCreateEnabledWebServer() {
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

	@Test
	@DisplayName("QueryDsl로 서버들을 조회한다.")
	void testFindAllByName() {
		// given
		String serverName = "gmoon";

		// when
		List<Server> servers = repository.getServers(serverName);

		// then
		assertThat(servers).size().isZero();
	}

	@Order(Integer.MAX_VALUE)
	@Test
	void testGetEnabledServers() {
		// when then
		assertThat(repository.getEnabledServers()).isEmpty();
	}
}
