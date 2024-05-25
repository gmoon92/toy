package com.gmoon.springsecuritycsrfaspect.csrf;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springsecuritycsrfaspect.csrf.token.BaseCsrfToken;
import com.gmoon.springsecuritycsrfaspect.csrf.vo.CsrfTokenResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/csrf")
@RequiredArgsConstructor
public class CsrfController {
	private final CsrfTokenRepository repository;

	@ResponseBody
	@PostMapping("/create")
	public CsrfTokenResponse create(HttpServletRequest request) {
		repository.saveTokenOnHttpSession(request);
		BaseCsrfToken token = repository.getToken(request);
		return CsrfTokenResponse.ok(token);
	}

	@ResponseBody
	@PostMapping("/delete")
	public CsrfTokenResponse delete(HttpServletRequest request) {
		BaseCsrfToken token = repository.getToken(request);

		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(repository.getSessionAttributeName());
		}
		return CsrfTokenResponse.ok(token);
	}
}
