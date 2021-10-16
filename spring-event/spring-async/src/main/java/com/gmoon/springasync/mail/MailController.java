package com.gmoon.springasync.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("mail")
public class MailController {
  private final MailService mailService;

  @PostMapping("/send/invite")
  public ResponseEntity<String> sendInviteMailFromMyGithub() {
    mailService.sendInviteMailFromMyGithubToAllUser();
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
