package com.gmoon.springframework.environment;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(EnvironmentConfigTest.ProfileTestConfig.class)
@TestPropertySource(value = "classpath:/environment.properties")
class EnvironmentConfigTest {

  @Autowired
  BaseRepository baseRepository;

  @Autowired
  Environment environment;

  @Value("${app.about}")
  String appAbout;

  @Test
  @DisplayName("spring.profiles.active=alpha")
  void getAlphaProperty() {
    // given
    String alphaProfileName = "alpha";

    // when
    String[] activeProfiles = environment.getActiveProfiles();
    log.info("Environment getActiveProfiles: {}", activeProfiles);
    log.info("Environment getDefaultProfiles: {}", environment.getDefaultProfiles());

    // then
    assertThat(activeProfiles)
            .contains(alphaProfileName);
  }

  @Test
  @DisplayName("app.about=spring-learning-app")
  void getAppAboutProperty() {
    // given
    String key = "app.about";

    // when
    String value = environment.getProperty(key);

    assertThat(value)
            .isEqualTo("spring-learning-app")
            .isEqualTo(appAbout);
  }

  @Test
  @DisplayName("프로파일에 Alpha로 설정, alpha로 설정한 AlphaRepository 클래스가 주입되는지")
  void profile_injection_alpha_bean() {
    assertThat(baseRepository)
            .isExactlyInstanceOf(AlphaRepository.class);
  }

  @TestConfiguration
  static class ProfileTestConfig {

    @Bean
    @Profile("alpha")
    public BaseRepository alphaRepository() {
      return new AlphaRepository();
    }

    @Bean
    @Profile("beta")
    public BaseRepository betaRepository() {
      return new AlphaRepository();
    }
  }

}