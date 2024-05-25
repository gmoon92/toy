package com.gmoon.springsecuritycsrfaspect.csrf;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.GetMapping;

import com.gmoon.springsecuritycsrfaspect.csrf.annotation.CSRF;
import com.gmoon.springsecuritycsrfaspect.csrf.annotation.CSRFTokenGenerator;
import com.gmoon.springsecuritycsrfaspect.csrf.token.BaseCsrfToken;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@Import(CsrfTokenAspectTest.TestConfig.class)
@ContextConfiguration(classes = CsrfTokenAspectTest.TestController.class)
class CsrfTokenAspectTest {

	@Autowired
	TestController testController;
	@Autowired
	CsrfTokenRepository csrfTokenRepository;

	@SpyBean
	CsrfTokenAspect csrfTokenAspect;
	@SpyBean
	HttpServletRequest request;

	@BeforeEach
	void setUp() {
		request.getSession().invalidate();
	}

	@Test
	@DisplayName("@CSRFTokenGenerator 메서드는 CSRF 토큰을 생성한다.")
	void generator() {
		// given
		testController.generatorToken();

		// when then
		then(csrfTokenAspect)
			 .should(times(1))
			 .generator();
	}

	@Test
	@DisplayName("@CSRFTokenGenerator 메서드는 예외가 발생되면 토큰을 생성되지 않는다.")
	void generator_error() {
		// given
		try {
			testController.throwsException();
		} catch (Exception e) {
			log.error("error!!", e);
		}

		// when then
		then(csrfTokenAspect)
			 .should(never())
			 .generator();
	}

	@Test
	@DisplayName("@CSRF 메서드는 클라이언트 요청이 CSRF 공격인지 판단한다.")
	void checkCsrfToken() {
		// given
		testController.generatorToken();
		BaseCsrfToken sessionToken = csrfTokenRepository.getToken(request);
		String token = sessionToken.getValue();

		// when
		when(request.getParameter("_csrf")).thenReturn(token);

		// then
		assertThat(testController.get())
			 .isEqualTo("info");
	}

	@Test
	@DisplayName("@CSRF 메서드는 HttpSession이 무효된 경우 에러를 반환한다.")
	void checkCsrfToken_error_when_invalidated_session() {
		// given
		request.getSession();

		// when then
		assertThatThrownBy(() -> testController.get())
			 .isInstanceOf(InvalidCsrfTokenException.class)
			 .hasMessage("http session csrf token is null.");
	}

	@Test
	@DisplayName("@CSRF 메서드는 요청 파라미터에 CSRF 토큰이 없을 경우 에러를 반환한다.")
	void checkCsrfToken_error_when_missing_request_token() {
		// given
		testController.generatorToken();

		// when then
		assertThatThrownBy(() -> testController.get())
			 .isInstanceOf(InvalidCsrfTokenException.class)
			 .hasMessage("request csrf token is null.");
	}

	@Test
	@DisplayName("@CSRF 메서드는 유효하지 않은 요청 CSRF 토큰일 없을 경우 에러를 반환한다.")
	void checkCsrfToken_error_invalid_request_token() {
		// given
		testController.generatorToken();

		// when
		when(request.getParameter("_csrf"))
			 .thenReturn("invalid token value");

		// then
		assertThatThrownBy(() -> testController.get())
			 .isInstanceOf(InvalidCsrfTokenException.class)
			 .hasMessage("csrf attack is prevented");
	}

	@TestConfiguration
	@Import(AopAutoConfiguration.class)
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	static class TestConfig {

		@Bean
		public MockHttpServletRequest httpServletRequest() {
			return new MockHttpServletRequest();
		}

		@Bean
		public CsrfTokenRepository csrfTokenRepository() {
			return new CsrfTokenRepository();
		}

		@Bean
		public CsrfTokenAspect csrfTokenAspect(MockHttpServletRequest request, CsrfTokenRepository repository) {
			return new CsrfTokenAspect(request, repository);
		}
	}

	@Controller
	public static class TestController {

		@CSRFTokenGenerator
		@GetMapping("/generator")
		public void generatorToken() {
			log.info("token generated.");
		}

		@CSRFTokenGenerator
		@GetMapping("/generator/error")
		public void throwsException() {
			throw new RuntimeException("error!!!");
		}

		@CSRF
		@GetMapping("/get")
		public String get() {
			log.info("get");
			return "info";
		}
	}
}
