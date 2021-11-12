package com.gmoon.springsecuritycsrfaspect.csrf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class CsrfTokenRepositoryTest {

  @Autowired
  HttpServletRequest request;

  @Autowired
  CsrfTokenRepository csrfTokenRepository;

  @Test
  @DisplayName("생성한 CSRF 토큰을 HttpSession에 저장한다.")
  void saveTokenOnHttpSession() {
    // given
    csrfTokenRepository.saveTokenOnHttpSession(request);

    // when
    CsrfToken token = csrfTokenRepository.getToken(request);

    // then
    HttpSession session = request.getSession();
    CsrfToken sessionToken = (CsrfToken) session.getAttribute(csrfTokenRepository.getSessionAttributeName());
    assertThat(token)
            .isEqualTo(sessionToken);
  }

  @Test
  @DisplayName("HttpSession이 없거나 저장된 csrf 토큰 값이 없을 경우 MissingCsrfToken token 값을 반환한다.")
  void missingCsrfTokenValue() {
    assertAll(() -> {
                // given
                HttpSession session = request.getSession();

                // when then
                assertThat(csrfTokenRepository.getTokenValue(session)).isEmpty();
              }
              , () -> assertThat(csrfTokenRepository.getTokenValue(null)).isEmpty());
  }
}