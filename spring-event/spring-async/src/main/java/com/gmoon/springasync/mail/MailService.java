package com.gmoon.springasync.mail;

import com.gmoon.springasync.member.Member;
import com.gmoon.springasync.member.MemberRepository;
import com.gmoon.springasync.server.Server;
import com.gmoon.springasync.server.ServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
  private final JavaMailSender mailSender;
  private final MailTemplate mailTemplate;
  private final ServerService serverService;
  private final MemberRepository memberRepository;

  @Async
  public void sendInviteMailFromMyGithubToAllUser() {
    try {
      Server githubServer = Server.createGithubBlogServer();
      String githubServerUrl = serverService.getWebServerWithoutPortUrl(githubServer);

      for (Member member : memberRepository.findAll()) {
        String email = member.getEmail();
        SimpleMailMessage message = mailTemplate.createInviteGithubMailMessage(email, githubServerUrl);
        mailSender.send(message);
        log.info("send {} mail", email);
      }
    } catch (Exception e) {
      throw new SendMailException(e);
    }
    log.info("send mail success...");
  }
}
