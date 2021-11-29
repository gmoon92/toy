package com.gmoon.springschedulingquartz.server;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ServerRepositoryTest {
  final ServerRepository repository;
  final EntityManager entityManager;

  @Test
  void testCount() {
    assertThat(repository.count()).isZero();
  }

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
    flushAndClear();

    // then
    assertThat(webServer)
            .isEqualTo(repository.findServerByName(serverName));
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }
}