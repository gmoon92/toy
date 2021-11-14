package com.gmoon.springsecuritycsrfaspect.csrf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/csrf")
@RequiredArgsConstructor
public class CsrfController {
  private final CsrfTokenRepository repository;

  @PostMapping("/create")
  public ResponseEntity<String> create(HttpServletRequest request) {
    repository.saveTokenOnHttpSession(request);
    String token = repository.getTokenValue(request);
    return ResponseEntity.ok(String.format("{\"csrf\": \"%s\" }", token));
  }

  @PostMapping("/delete")
  public ResponseEntity<String> delete(HttpSession session) {
    if (session != null) {
      session.removeAttribute(repository.getSessionAttributeName());
    }
    return ResponseEntity.ok("{\"message\": \"csrf token delete.\"");
  }
}