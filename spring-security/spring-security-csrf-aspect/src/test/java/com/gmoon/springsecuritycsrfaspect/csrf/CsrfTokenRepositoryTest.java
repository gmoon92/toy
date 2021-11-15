package com.gmoon.springsecuritycsrfaspect.csrf;

import com.gmoon.springsecuritycsrfaspect.csrf.token.BaseCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.token.MissingCsrfToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CsrfTokenRepositoryTest {

  @Autowired HttpServletRequest request;
  @Autowired CsrfTokenRepository csrfTokenRepository;

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
  @DisplayName("HttpSession에 저장된 csrf 토큰 값이 없을 경우 MissingCsrfToken token 값을 반환한다.")
  void missingCsrfTokenValue() {
    // given
    BaseCsrfToken sessionToken = csrfTokenRepository.getToken(request);

    // when then
    assertThat(sessionToken.getValue())
            .isEqualTo(MissingCsrfToken.INSTANCE.getValue());
  }
}