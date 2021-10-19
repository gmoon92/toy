package com.gmoon.springasync.mail;

import com.gmoon.springasync.member.Member;
import com.gmoon.springasync.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
  private final JavaMailSender mailSender;
  private final MailTemplate mailTemplate;

  private final MemberRepository memberRepository;

  @Async
  public void sendInviteMailFrom(final String publicUrl) {
    memberRepository.streamFindAll()
            .map(Member::getEmail)
            .filter(StringUtils::isNotBlank)
            .map(convertInviteMailVO(publicUrl))
            .forEach(this::sendMail);
  }

  private void sendMail(InviteMailVO mailVO) {
    try {
      String email = mailVO.getReceiver();
      String inviteServerUrl = mailVO.getInviteServerUrl();
      SimpleMailMessage message = mailTemplate.createInviteGithubMailMessage(email, inviteServerUrl);
      mailSender.send(message);
    } catch (Exception e) {
      throw new SendMailException(e);
    }
  }

  private Function<String, InviteMailVO> convertInviteMailVO(String publicUrl) {
    return receiver -> InviteMailVO.createNew(receiver, publicUrl);
  }
}
